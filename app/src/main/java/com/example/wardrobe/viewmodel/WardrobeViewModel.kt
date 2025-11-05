package com.example.wardrobe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.Location
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.ui.components.TagUiModel
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

data class UiState(
    val memberName: String = "",
    val tags: List<TagUiModel> = emptyList(),
    val locations: List<Location> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val query: String = "",
    val items: List<ClothingItem> = emptyList(),
    val currentView: ViewType = ViewType.IN_USE
)

class WardrobeViewModel(
    private val repo: WardrobeRepository,
    private val memberId: Long
) : ViewModel() {
    private val selectedTagIds = MutableStateFlow<Set<Long>>(emptySet())
    private val query = MutableStateFlow("")
    private val currentView = MutableStateFlow(ViewType.IN_USE)

    val uiState: StateFlow<UiState> = combine(
        selectedTagIds,
        query,
        currentView
    ) { sel, q, view ->
        Triple(sel, q, view)
    }.flatMapLatest { (sel, q, view) ->
        val itemsFlow = repo.observeItems(memberId, sel.toList(), q)
        val tagsFlow = repo.observeTagsWithCounts(memberId, view == ViewType.STORED)

        combine(
            itemsFlow,
            repo.getMember(memberId),
            tagsFlow,
            repo.observeLocations()
        ) { items, member, tagsWithCount, locations ->
            val filteredItems = items.filter { item ->
                if (view == ViewType.IN_USE) !item.stored else item.stored
            }

            UiState(
                memberName = member?.name ?: "",
                tags = tagsWithCount.map { TagUiModel(it.tagId, it.name, it.count) },
                locations = locations,
                selectedTagIds = sel,
                query = q,
                items = filteredItems,
                currentView = view
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    init {
        // Ensure some default tags exist the first time
        viewModelScope.launch {
            repo.ensureDefaultTags(listOf("Spring/Autumn", "Summer", "Winter", "Hat", "Top", "Pants", "Shoes", "Jumpsuit"))
        }
    }

    fun setViewType(viewType: ViewType) {
        currentView.value = viewType
    }

    suspend fun getOrCreateTag(name: String): Long {
        return repo.getOrCreateTag(name)
    }

    fun toggleTag(id: Long) {
        selectedTagIds.value = selectedTagIds.value.toMutableSet().also { set ->
            if (!set.add(id)) set.remove(id)
        }
    }

    fun setQuery(q: String) { query.value = q }

    fun addLocation(name: String) = viewModelScope.launch {
        repo.addLocation(name)
    }

    fun deleteLocation(locationId: Long) = viewModelScope.launch {
        repo.deleteLocation(locationId)
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
}
