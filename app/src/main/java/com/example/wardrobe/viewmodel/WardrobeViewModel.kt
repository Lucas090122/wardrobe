package com.example.wardrobe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.TagWithCount
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.ui.components.TagUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for a specific member's Wardrobe
 * - Holds UI state for Home (tags, selection, query, items)
 * - Provides save API used by Edit screen later
 */
data class UiState(
    val memberName: String = "",
    val tags: List<TagUiModel> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val query: String = "",
    val items: List<ClothingItem> = emptyList()
)

class WardrobeViewModel(
    private val repo: WardrobeRepository,
    private val memberId: Long
) : ViewModel() {
    private val selectedTagIds = MutableStateFlow<Set<Long>>(emptySet())
    private val query = MutableStateFlow("")

    val uiState: StateFlow<UiState> = combine(
        repo.getMember(memberId),
        repo.observeTagsWithCounts(memberId),
        selectedTagIds,
        query
    ) { member, tagsWithCount, sel, q ->
        val clothingItemsFlow = repo.observeItems(memberId, sel.toList(), q)
        clothingItemsFlow.map {
            UiState(
                memberName = member?.name ?: "",
                tags = tagsWithCount.map { TagUiModel(it.tagId, it.name, it.count) },
                selectedTagIds = sel,
                query = q,
                items = it
            )
        }
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    init {
        // Ensure some default tags exist the first time
        viewModelScope.launch {
            repo.ensureDefaultTags(listOf("Spring/Autumn", "Summer", "Winter", "Hat", "Top", "Pants", "Shoes", "Jumpsuit"))
        }
    }

    fun toggleTag(id: Long) {
        selectedTagIds.value = selectedTagIds.value.toMutableSet().also { set ->
            if (!set.add(id)) set.remove(id)
        }
    }

    fun setQuery(q: String) { query.value = q }

    fun saveItem(
        itemId: Long? = null,
        description: String,
        imageUri: String?,
        tagIds: List<Long>
    ) = viewModelScope.launch {
        repo.saveItem(memberId, itemId, description, imageUri, tagIds)
    }

    fun itemFlow(itemId: Long) = repo.observeItem(itemId)

    fun deleteItem(itemId: Long) = viewModelScope.launch {
        repo.deleteItem(itemId)
    }

    suspend fun getOrCreateTag(name: String): Long {
        return repo.getOrCreateTag(name)
    }
}