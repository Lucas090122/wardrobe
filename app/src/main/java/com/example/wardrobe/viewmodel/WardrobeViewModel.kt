package com.example.wardrobe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.Location
import com.example.wardrobe.data.Member // Added import
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.ui.components.TagUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ViewType {
    IN_USE,
    STORED
}

sealed class DialogEffect {
    data object Hidden : DialogEffect()
    sealed class DeleteLocation : DialogEffect() {
        data class AdminConfirm(val locationId: Long, val itemCount: Int) : DeleteLocation()
        data class PreventDelete(val itemCount: Int) : DeleteLocation()
    }
    sealed class DeleteTag : DialogEffect() {
        data class AdminConfirm(val tagId: Long, val itemCount: Int) : DeleteTag()
        data class PreventDelete(val itemCount: Int) : DeleteTag()
    }
}

data class UiState(
    val memberName: String = "",
    val members: List<Member> = emptyList(), // Added
    val tags: List<TagUiModel> = emptyList(),
    val locations: List<Location> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val query: String = "",
    val items: List<ClothingItem> = emptyList(),
    val currentView: ViewType = ViewType.IN_USE,
    val isAdminMode: Boolean = false,
    val dialogEffect: DialogEffect = DialogEffect.Hidden
)

private data class VmCoreState(
    val sel: Set<Long>,
    val q: String,
    val view: ViewType,
    val isAdmin: Boolean,
    val dialogEffect: DialogEffect
)

private val DEFAULT_TAGS = setOf("Spring/Autumn", "Summer", "Winter", "Hat", "Top", "Pants", "Shoes", "Jumpsuit")

class WardrobeViewModel(
    private val repo: WardrobeRepository,
    private val memberId: Long
) : ViewModel() {
    private val selectedTagIds = MutableStateFlow<Set<Long>>(emptySet())
    private val query = MutableStateFlow("")
    private val currentView = MutableStateFlow(ViewType.IN_USE)
    private val dialogEffect = MutableStateFlow<DialogEffect>(DialogEffect.Hidden)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState> = combine(
        combine(selectedTagIds, query, currentView) { sel, q, view -> Triple(sel, q, view) },
        combine(repo.settings.isAdminMode, dialogEffect) { isAdmin, effect -> isAdmin to effect }
    ) { (sel, q, view), (isAdmin, effect) ->
        VmCoreState(sel, q, view, isAdmin, effect)
    }.flatMapLatest { state ->
        val itemsFlow = repo.observeItems(memberId, state.sel.toList(), state.q)
        val tagsFlow = repo.observeTagsWithCounts(memberId, state.view == ViewType.STORED)

        combine(
            itemsFlow,
            repo.getMember(memberId),
            tagsFlow,
            repo.observeLocations(),
            repo.getAllMembers() // Added
        ) { items, member, tagsWithCount, locations, allMembers -> // Modified signature
            val filteredItems = items.filter { item ->
                if (state.view == ViewType.IN_USE) !item.stored else item.stored
            }

            UiState(
                memberName = member?.name ?: "",
                members = allMembers, // Added
                tags = tagsWithCount.map { tag ->
                    TagUiModel(
                        id = tag.tagId,
                        name = tag.name,
                        count = tag.count,
                        isDeletable = tag.name !in DEFAULT_TAGS
                    )
                },
                locations = locations,
                selectedTagIds = state.sel,
                query = state.q,
                items = filteredItems,
                currentView = state.view,
                isAdminMode = state.isAdmin,
                dialogEffect = state.dialogEffect
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    init {
        // Ensure some default tags exist the first time
        viewModelScope.launch {
            repo.ensureDefaultTags(DEFAULT_TAGS.toList())
        }
    }

    fun setViewType(viewType: ViewType) {
        currentView.value = viewType
    }

    fun clearTagSelection() {
        selectedTagIds.value = emptySet()
    }

    suspend fun getOrCreateTag(name: String): Long {
        return repo.getOrCreateTag(name)
    }

    fun toggleTag(id: Long) {
        selectedTagIds.value = selectedTagIds.value.toMutableSet().also { set ->
            if (!set.add(id)) set.remove(id)
        }
    }

    fun setQuery(q: String) {
        query.value = q
    }

    fun addLocation(name: String) = viewModelScope.launch {
        repo.addLocation(name)
    }

    fun deleteLocation(locationId: Long) = viewModelScope.launch {
        val count = repo.getItemCountForLocation(locationId)
        if (count == 0) {
            repo.deleteLocation(locationId)
            return@launch
        }

        if (uiState.value.isAdminMode) {
            dialogEffect.value = DialogEffect.DeleteLocation.AdminConfirm(locationId, count)
        } else {
            dialogEffect.value = DialogEffect.DeleteLocation.PreventDelete(count)
        }
    }

    fun forceDeleteLocation(locationId: Long) = viewModelScope.launch {
        repo.deleteLocation(locationId)
        clearDialogEffect()
    }

    fun deleteTag(tagId: Long) = viewModelScope.launch {
        val count = repo.getItemCountForTag(tagId)
        if (count == 0) {
            repo.deleteTag(tagId)
            return@launch
        }

        if (uiState.value.isAdminMode) {
            dialogEffect.value = DialogEffect.DeleteTag.AdminConfirm(tagId, count)
        } else {
            dialogEffect.value = DialogEffect.DeleteTag.PreventDelete(count)
        }
    }

    fun forceDeleteTag(tagId: Long) = viewModelScope.launch {
        repo.deleteTag(tagId)
        clearDialogEffect()
    }

    fun clearDialogEffect() {
        dialogEffect.value = DialogEffect.Hidden
    }

    fun saveItem(
        itemId: Long? = null,
        description: String,
        imageUri: String?,
        tagIds: List<Long>,
        stored: Boolean,
        locationId: Long?
    ) = viewModelScope.launch {
        repo.saveItem(memberId, itemId, description, imageUri, tagIds, stored, locationId)
    }

    fun itemFlow(itemId: Long) = repo.observeItem(itemId)

    fun deleteItem(itemId: Long) = viewModelScope.launch {
        repo.deleteItem(itemId)
    }

    fun setAdminMode(isAdmin: Boolean) = viewModelScope.launch {
        repo.settings.setAdminMode(isAdmin)
    }

    fun transferItem(itemId: Long, newOwnerMemberId: Long) = viewModelScope.launch {
        repo.transferItem(itemId, newOwnerMemberId)
    }
}