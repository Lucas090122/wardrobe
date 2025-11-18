package com.example.wardrobe.data

import com.example.wardrobe.data.TransferHistoryDetails // Added import
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class WardrobeRepository(
    private val dao: ClothesDao,
    val settings: SettingsRepository
) {
    // Member functions
    fun getAllMembers(): Flow<List<Member>> = dao.getAllMembers()

    fun getMember(memberId: Long): Flow<Member?> = dao.getMember(memberId)

    suspend fun createMember(
        name: String,
        gender: String,
        age: Int,
        birthDate: Long?
    ): Long {
        return dao.insertMember(
            Member(
                name = name,
                gender = gender,
                age = age,
                birthDate = birthDate
            )
        )
    }

    // Location functions
    fun observeLocations(): Flow<List<Location>> = dao.getAllLocations()

    suspend fun addLocation(name: String): Long {
        return dao.insertLocation(Location(name = name))
    }

    suspend fun deleteLocation(locationId: Long) {
        dao.deleteLocation(locationId)
    }

    suspend fun getItemCountForLocation(locationId: Long): Int {
        return dao.getItemCountForLocation(locationId)
    }

    // Tag functions
    fun observeTags() = dao.allTags()

    fun observeTagsWithCounts(memberId: Long, isStored: Boolean) =
        dao.getTagsWithCounts(memberId, isStored)

    suspend fun getOrCreateTag(name: String): Long {
        val sanitizedName = name.trim()
        if (sanitizedName.isEmpty()) return 0L

        val existing = dao.getTagByName(sanitizedName)
        return existing?.tagId ?: dao.upsertTag(Tag(name = sanitizedName))
    }

    suspend fun deleteTag(tagId: Long) {
        dao.deleteTag(tagId)
    }

    suspend fun getItemCountForTag(tagId: Long): Int {
        return dao.getItemCountForTag(tagId)
    }

    // Item functions
    fun observeItems(memberId: Long, selectedTagIds: List<Long>, query: String?, season: Season?) =
        if (selectedTagIds.isEmpty()) dao.itemsStream(memberId, query, season?.name)
        else dao.itemsByTagsStream(memberId, selectedTagIds, query, season?.name)

    fun observeItem(itemId: Long) = dao.itemWithTags(itemId)

    suspend fun saveItem(
        memberId: Long, // Required owner
        itemId: Long?,
        description: String,
        imageUri: String?,
        tagIds: List<Long>,
        stored: Boolean,
        locationId: Long?,
        // --- V2.4 new fields ---
        category: String,
        warmthLevel: Int,
        occasions: String,
        isWaterproof: Boolean,
        color: String,
        sizeLabel: String?,
        isFavorite: Boolean,
        season: Season
    ): Long {
        val existingItem = if (itemId != null && itemId != 0L) {
            // We need to use firstOrNull() as itemWithTags returns a Flow
            dao.itemWithTags(itemId).firstOrNull()?.item
        } else {
            null
        }

        val newClothingItem = ClothingItem(
            itemId = itemId ?: 0,
            ownerMemberId = memberId,
            description = description,
            imageUri = imageUri,
            stored = stored,
            locationId = locationId,
            createdAt = existingItem?.createdAt ?: System.currentTimeMillis(),

            // --- V2.4 new fields ---
            category = category,
            warmthLevel = warmthLevel,
            occasions = occasions,
            isWaterproof = isWaterproof,
            color = color,
            lastWornAt = existingItem?.lastWornAt ?: 0, // Preserve existing lastWornAt
            isFavorite = isFavorite,
            season = season,
            sizeLabel = sizeLabel
        )

        val id = dao.upsertItem(newClothingItem)
            .let { if (itemId != null && itemId != 0L) itemId else it }

        dao.clearTagsForItem(id)
        dao.upsertCrossRefs(tagIds.map { ClothingTagCrossRef(id, it) })
        return id
    }

    suspend fun ensureDefaultTags(names: List<String>) {
        val existing = dao.tagNamesOnce().toSet()
        names.filter { it !in existing }
            .forEach { dao.upsertTag(Tag(name = it)) }
    }

    suspend fun deleteItem(itemId: Long) {
        dao.clearTagsForItem(itemId)
        dao.deleteItemById(itemId)
    }

    suspend fun transferItem(itemId: Long, newOwnerMemberId: Long) {
        dao.updateItemOwner(itemId, newOwnerMemberId)
    }

    suspend fun recordTransferHistory(transferHistory: TransferHistory) {
        dao.insertTransferHistory(transferHistory)
    }

    fun getAllTransferHistoryDetails(): Flow<List<TransferHistoryDetails>> {
        return dao.getAllTransferHistoryDetails()
    }

    fun getCountByMember(): Flow<List<NameCount>> = dao.getCountByMember()
    fun getCountBySeason(): Flow<List<SeasonCount>> = dao.getCountBySeason()
    fun getCountByCategory(): Flow<List<CategoryCount>> = dao.getCountByCategory()
}