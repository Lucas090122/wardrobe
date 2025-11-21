package com.example.wardrobe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobe.data.GrowthSizeTable
import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.Location
import com.example.wardrobe.data.Member
import com.example.wardrobe.data.Season
import com.example.wardrobe.data.TransferHistory
import com.example.wardrobe.data.TransferHistoryDetails
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.ui.components.TagUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Logical view of the main wardrobe list:
 * - IN_USE: items currently in rotation
 * - STORED: items stored away (e.g. in boxes)
 */
enum class ViewType {
    IN_USE,
    STORED
}

/**
 * One-shot dialog effects that the UI should respond to.
 *
 * This is used instead of plain booleans to:
 * - Distinguish different dialogs (delete tag vs location)
 * - Pass extra information (item count, ids, etc.)
 */
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

/**
 * Full UI state for the wardrobe screen.
 *
 * Aggregates:
 * - Member info
 * - Items + filters
 * - Tags, locations
 * - Admin mode & AI toggle
 * - Outdated size warnings
 * - Dialog effects
 */
data class UiState(
    val memberName: String = "",
    val members: List<Member> = emptyList(),
    val tags: List<TagUiModel> = emptyList(),
    val locations: List<Location> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val selectedSeason: Season? = null,
    val query: String = "",
    val items: List<ClothingItem> = emptyList(),
    val currentView: ViewType = ViewType.IN_USE,
    val isAdminMode: Boolean = false,
    val dialogEffect: DialogEffect = DialogEffect.Hidden,
    val outdatedItemIds: Set<Long> = emptySet(),
    val outdatedCount: Int = 0,
    val isAiEnabled: Boolean = false
)

/**
 * Internal state used to drive item queries.
 * Keeps only the minimal data needed for flows.
 */
private data class VmCoreState(
    val sel: Set<Long>,
    val q: String,
    val view: ViewType,
    val season: Season?,
    val isAdmin: Boolean,
    val dialogEffect: DialogEffect,
    val isAiEnabled: Boolean
)

/**
 * Intermediate settings state built from base flows + settings repository.
 */
private data class CoreSettingsState(
    val selectedTagIds: Set<Long>,
    val query: String,
    val currentView: ViewType,
    val selectedSeason: Season?,
    val isAdminMode: Boolean,
    val isAiEnabled: Boolean
)

/**
 * Default tags that should not be deletable.
 */
private val DEFAULT_TAGS = setOf("Hat", "Top", "Pants", "Shoes")

/**
 * Main ViewModel for a single member's wardrobe.
 *
 * Responsibilities:
 * - Manage filters (tags, seasons, query, view type)
 * - Expose combined UI state from repository flows
 * - Handle admin-only destructive actions with confirmation dialogs
 * - Compute outdated-size hints based on member age & growth table
 * - Coordinate transfer history logging
 */
class WardrobeViewModel(
    private val repo: WardrobeRepository,
    private val memberId: Long
) : ViewModel() {

    // Local filter states
    private val selectedTagIds = MutableStateFlow<Set<Long>>(emptySet())
    private val query = MutableStateFlow("")
    private val currentView = MutableStateFlow(ViewType.IN_USE)
    private val selectedSeason = MutableStateFlow<Season?>(null)
    private val dialogEffect = MutableStateFlow<DialogEffect>(DialogEffect.Hidden)

    /**
     * Combine basic filter flows with admin mode from SettingsRepository.
     */
    private val coreSettingsFlow = combine(
        selectedTagIds,
        query,
        currentView,
        selectedSeason,
        repo.settings.isAdminMode
    ) { sel, q, view, season, isAdmin ->
        CoreSettingsState(
            selectedTagIds = sel,
            query = q,
            currentView = view,
            selectedSeason = season,
            isAdminMode = isAdmin,
            isAiEnabled = false
        )
    }

    /**
     * Extend core settings with AI enabled flag from SettingsRepository.
     */
    private val coreWithAiFlow = combine(
        coreSettingsFlow,
        repo.settings.isAiEnabled
    ) { core, isAiEnabled ->
        core.copy(isAiEnabled = isAiEnabled)
    }

    /**
     * Public UI state exposed as a hot StateFlow.
     *
     * Flow chain:
     *  coreWithAiFlow + dialogEffect
     *    → VmCoreState
     *      → flatMapLatest to item & tag & location & member flows
     *        → UiState
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState> = combine(
        coreWithAiFlow,
        dialogEffect
    ) { core, effect ->
        VmCoreState(
            sel = core.selectedTagIds,
            q = core.query,
            view = core.currentView,
            season = core.selectedSeason,
            isAdmin = core.isAdminMode,
            dialogEffect = effect,
            isAiEnabled = core.isAiEnabled
        )
    }.flatMapLatest { state ->
        val itemsFlow = repo.observeItems(memberId, state.sel.toList(), state.q, state.season)
        val tagsFlow = repo.observeTagsWithCounts(memberId, state.view == ViewType.STORED)

        combine(
            itemsFlow,
            repo.getMember(memberId),
            tagsFlow,
            repo.observeLocations(),
            repo.getAllMembers()
        ) { items, member, tagsWithCount, locations, allMembers ->
            val filteredItems = items.filter { item ->
                if (state.view == ViewType.IN_USE) !item.stored else item.stored
            }

            val outdatedIds: Set<Long> =
                if (member != null && computeAgeYears(member) < 18) {
                    filteredItems
                        .filter { item -> isItemOutdatedForMember(item, member) }
                        .map { it.itemId }
                        .toSet()
                } else {
                    emptySet()
                }

            UiState(
                memberName = member?.name ?: "",
                members = allMembers,
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
                selectedSeason = state.season,
                query = state.q,
                items = filteredItems,
                currentView = state.view,
                isAdminMode = state.isAdmin,
                dialogEffect = state.dialogEffect,
                outdatedItemIds = outdatedIds,
                outdatedCount = outdatedIds.size,
                isAiEnabled = state.isAiEnabled
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    init {
        // Ensure some default tags exist the first time this ViewModel is created
        viewModelScope.launch {
            repo.ensureDefaultTags(DEFAULT_TAGS.toList())
        }
    }

    // --------------------------------------------------------------------
    // Filter state updates
    // --------------------------------------------------------------------

    fun setViewType(viewType: ViewType) {
        currentView.value = viewType
    }

    fun clearTagSelection() {
        selectedTagIds.value = emptySet()
    }

    fun setSeasonFilter(season: Season) {
        if (selectedSeason.value == season) {
            selectedSeason.value = null
        } else {
            selectedSeason.value = season
        }
    }

    fun clearSeasonFilter() {
        selectedSeason.value = null
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

    // --------------------------------------------------------------------
    // Location & Tag management with admin rules
    // --------------------------------------------------------------------

    fun addLocation(name: String) = viewModelScope.launch {
        repo.addLocation(name)
    }

    fun deleteLocation(locationId: Long) = viewModelScope.launch {
        val count = repo.getItemCountForLocation(locationId)
        if (count == 0) {
            repo.deleteLocation(locationId)
            return@launch
        }

        // Location in use: behavior depends on admin mode
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

        // Tag in use: behavior depends on admin mode
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

    // --------------------------------------------------------------------
    // CRUD operations for clothing items
    // --------------------------------------------------------------------

    fun saveItem(
        itemId: Long? = null,
        description: String,
        imageUri: String?,
        tagIds: List<Long>,
        stored: Boolean,
        locationId: Long?,
        category: String,
        warmthLevel: Int,
        occasions: String,
        isWaterproof: Boolean,
        color: String,
        sizeLabel: String?,
        isFavorite: Boolean,
        season: Season
    ) = viewModelScope.launch {
        repo.saveItem(
            memberId = memberId,
            itemId = itemId,
            description = description,
            imageUri = imageUri,
            tagIds = tagIds,
            stored = stored,
            locationId = locationId,
            category = category,
            warmthLevel = warmthLevel,
            occasions = occasions,
            isWaterproof = isWaterproof,
            color = color,
            sizeLabel = sizeLabel,
            isFavorite = isFavorite,
            season = season
        )
    }

    fun itemFlow(itemId: Long) = repo.observeItem(itemId)

    fun deleteItem(itemId: Long) = viewModelScope.launch {
        repo.deleteItem(itemId)
    }

    fun setAdminMode(isAdmin: Boolean) = viewModelScope.launch {
        repo.settings.setAdminMode(isAdmin)
    }

    // --------------------------------------------------------------------
    // Outdated size logic
    // --------------------------------------------------------------------

    /**
     * Compute age in years from member data.
     * Prefer birthDate if present, otherwise fall back to static age.
     */
    private fun computeAgeYears(member: Member): Int {
        val now = System.currentTimeMillis()
        return if (member.birthDate != null && member.birthDate > 0L) {
            GrowthSizeTable.ageFromBirthMillis(member.birthDate, now)
        } else {
            member.age
        }
    }

    /**
     * Determine whether a given clothing item is too small for the member,
     * based on growth size tables and numeric size extracted from sizeLabel.
     */
    private fun isItemOutdatedForMember(item: ClothingItem, member: Member): Boolean {
        val ageYears = computeAgeYears(member)
        if (ageYears >= 18) return false
        if (item.category !in listOf("TOP", "PANTS", "SHOES")) return false

        val rec = GrowthSizeTable.getRecommendedSize(member.gender, ageYears)

        val numericSize = item.sizeLabel
            ?.filter { it.isDigit() }
            ?.toIntOrNull()
            ?: return false

        return when (item.category) {
            "TOP"   -> rec.top   != null && numericSize < rec.top
            "PANTS" -> rec.pants != null && numericSize < rec.pants
            "SHOES" -> rec.shoes != null && numericSize < rec.shoes
            else    -> false
        }
    }

    // --------------------------------------------------------------------
    // Transfer & history
    // --------------------------------------------------------------------

    /**
     * Transfer an item from one member to another and record the transfer history.
     */
    fun transferItem(itemId: Long, newOwnerMemberId: Long) = viewModelScope.launch {
        // Get the current item to retrieve its ownerMemberId before transfer
        val currentItem = repo.observeItem(itemId).firstOrNull()?.item
        val sourceMemberId = currentItem?.ownerMemberId ?: return@launch // Should not be null

        repo.transferItem(itemId, newOwnerMemberId)
        repo.recordTransferHistory(
            TransferHistory(
                itemId = itemId,
                sourceMemberId = sourceMemberId,
                targetMemberId = newOwnerMemberId
            )
        )
    }

    /**
     * Transfer history stream, exposed for history UI.
     */
    val transferHistory: StateFlow<List<TransferHistoryDetails>> =
        repo.getAllTransferHistoryDetails()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Mark a recommended outfit as worn today.
     * Delegates to repository which updates lastWornAt.
     */
    fun markOutfitAsWorn(items: List<ClothingItem>) {
        viewModelScope.launch {
            repo.markItemsAsWorn(items)
        }
    }
}