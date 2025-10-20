package com.example.wardrobe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.Tag
import com.example.wardrobe.data.WardrobeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Xiao Wang's Wardrobe
 * - Holds UI state for Home (tags, selection, query, items)
 * - Provides save API used by Edit screen later
 */
data class UiState(
    val tags: List<Tag> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val query: String = "",
    val items: List<ClothingItem> = emptyList()
)

class WardrobeViewModel(private val repo: WardrobeRepository) : ViewModel() {
    private val selectedTagIds = MutableStateFlow<Set<Long>>(emptySet())
    private val query = MutableStateFlow("")

    val uiState: StateFlow<UiState> = combine(
        repo.observeTags().map { it.distinctBy { t -> t.name } },
        selectedTagIds,
        query
    ) { tags, sel, q -> Triple(tags, sel, q) }
        .flatMapLatest { (tags, sel, q) ->
            repo.observeItems(sel.toList(), q).map { items ->
                UiState(tags = tags, selectedTagIds = sel, query = q, items = items)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    init {
        // Ensure some default tags exist the first time
        viewModelScope.launch {
            repo.ensureDefaultTags(listOf("春秋", "夏装", "冬装", "帽子", "上衣", "裤子", "鞋子", "连体"))
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
        repo.saveItem(itemId, description, imageUri, tagIds)
    }

    fun itemFlow(itemId: Long) = repo.observeItem(itemId)

    fun deleteItem(itemId: Long) = viewModelScope.launch {
        repo.deleteItem(itemId)
    }
}