package com.example.wardrobe.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Repository layer that sits between the ViewModel and DAO.
 *
 * Responsibilities:
 *  - Provide clean API for UI layer
 *  - Coordinate multi-table operations (tags, locations, history, items)
 *  - Maintain business logic (default tags, transfer history, size checks)
 *  - Manage settings via SettingsRepository
 *
 * This class should contain *business meaning* logic, not UI logic.
 */
class WardrobeRepository(
    private val dao: ClothesDao,
    val settings: SettingsRepository
) {

    // ---------------------------------------------------------------------
    // Member operations
    // ---------------------------------------------------------------------

    fun getAllMembers(): Flow<List<Member>> = dao.getAllMembers()

    fun getMember(memberId: Long): Flow<Member?> = dao.getMember(memberId)

    /**
     * Create a new member.
     * Owners of clothing items must exist, so inserting a member is a core operation.
     */
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

    suspend fun deleteMember(memberId: Long) {
        dao.deleteMemberById(memberId)
    }

    suspend fun updateMember(
        memberId: Long,
        name: String,
        gender: String,
        age: Int,
        birthDate: Long?
    ) {
        dao.updateMember(
            Member(
                memberId = memberId,
                name = name,
                gender = gender,
                age = age,
                birthDate = birthDate
            )
        )
    }

    // ---------------------------------------------------------------------
    // Location operations
    // ---------------------------------------------------------------------

    fun observeLocations(): Flow<List<Location>> = dao.getAllLocations()

    suspend fun addLocation(name: String): Long {
        return dao.insertLocation(Location(name = name))
    }

    suspend fun deleteLocation(locationId: Long) {
        dao.deleteLocation(locationId)
    }

    /**
     * Count how many items are currently stored under a given location.
     * Used to prevent accidental deletions in normal mode.
     */
    suspend fun getItemCountForLocation(locationId: Long): Int {
        return dao.getItemCountForLocation(locationId)
    }

    // ---------------------------------------------------------------------
    // Tag operations
    // ---------------------------------------------------------------------

    /**
     * Returns tags + usage count for filtering UI.
     * The 'isStored' flag affects which items are included in counts.
     */
    fun observeTagsWithCounts(memberId: Long, isStored: Boolean) =
        dao.getTagsWithCounts(memberId, isStored)

    /**
     * Get or create a tag by name.
     * Ensures:
     *  - No duplicates
     *  - Clean trimmed naming
     */
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

    // ---------------------------------------------------------------------
    // Clothing item operations
    // ---------------------------------------------------------------------

    /**
     * Observes item list with filtering:
     * - Tags
     * - Search query
     * - Season
     *
     * If no tags are selected, use basic query stream for efficiency.
     * Otherwise use tag-joined version.
     */
    fun observeItems(memberId: Long, selectedTagIds: List<Long>, query: String?, season: Season?) =
        if (selectedTagIds.isEmpty()) dao.itemsStream(memberId, query, season?.name)
        else dao.itemsByTagsStream(memberId, selectedTagIds, query, season?.name)

    fun observeItem(itemId: Long) = dao.itemWithTags(itemId)

    suspend fun getItemsByIds(ids: Set<Long>): List<ClothingItem> = dao.getItemsByIds(ids)

    /**
     * Create or update a clothing item.
     *
     * Steps:
     *  1. Load existing item (if editing)
     *  2. Preserve createdAt & lastWornAt
     *  3. Upsert main item
     *  4. Replace tag cross-ref entries
     */
    suspend fun saveItem(
        memberId: Long,
        itemId: Long?,
        description: String,
        imageUri: String?,
        tagIds: List<Long>,
        stored: Boolean,
        locationId: Long?,
        // --- V2.4 additional fields ---
        category: String,
        warmthLevel: Int,
        occasions: String,
        isWaterproof: Boolean,
        color: String,
        sizeLabel: String?,
        isFavorite: Boolean,
        season: Season
    ): Long {

        // Load existing item if editing
        val existingItem = if (itemId != null && itemId != 0L) {
            dao.itemWithTags(itemId).firstOrNull()?.item
        } else null

        val newClothingItem = ClothingItem(
            itemId = itemId ?: 0,
            ownerMemberId = memberId,
            description = description,
            imageUri = imageUri,
            stored = stored,
            locationId = locationId,
            createdAt = existingItem?.createdAt ?: System.currentTimeMillis(),

            // Additional V2.4 details
            category = category,
            warmthLevel = warmthLevel,
            occasions = occasions,
            isWaterproof = isWaterproof,
            color = color,
            lastWornAt = existingItem?.lastWornAt ?: 0,   // Preserve usage history
            isFavorite = isFavorite,
            season = season,
            sizeLabel = sizeLabel
        )

        // Upsert main item
        val id = dao.upsertItem(newClothingItem)
            .let { if (itemId != null && itemId != 0L) itemId else it }

        // Replace tag relations completely
        dao.clearTagsForItem(id)
        dao.upsertCrossRefs(tagIds.map { ClothingTagCrossRef(id, it) })

        return id
    }

    /**
     * Ensure default predefined tags exist (e.g., "Top", "Pants", etc.)
     */
    suspend fun ensureDefaultTags(names: List<String>) {
        val existing = dao.tagNamesOnce().toSet()
        names.filter { it !in existing }
            .forEach { dao.upsertTag(Tag(name = it)) }
    }

    suspend fun deleteItem(itemId: Long) {
        dao.clearTagsForItem(itemId)
        dao.deleteItemById(itemId)
    }

    // ---------------------------------------------------------------------
    // Item transfer & history
    // ---------------------------------------------------------------------

    suspend fun transferItem(itemId: Long, newOwnerMemberId: Long) {
        dao.updateItemOwner(itemId, newOwnerMemberId)
    }

    suspend fun recordTransferHistory(transferHistory: TransferHistory) {
        dao.insertTransferHistory(transferHistory)
    }

    fun getAllTransferHistoryDetails(): Flow<List<TransferHistoryDetails>> {
        return dao.getAllTransferHistoryDetails()
    }

    // ---------------------------------------------------------------------
    // Outdated size detection (kids outgrew clothes)
    // ---------------------------------------------------------------------

    /**
     * Counts how many items for a member are outdated based on:
     *  - Age detection (from birthDate OR age field)
     *  - Recommended size table
     *  - Clothing category restrictions
     */
    suspend fun countOutdatedItems(memberId: Long): Int {
        val member = dao.getMember(memberId).firstOrNull() ?: return 0

        val now = System.currentTimeMillis()

        // Determine age using birthDate if possible; fallback to static age field
        val ageYears: Int = try {
            val birthDateField = Member::class.java.getDeclaredField("birthDate")
            birthDateField.isAccessible = true
            val birthMillis = birthDateField.get(member) as? Long

            if (birthMillis != null && birthMillis > 0L) {
                GrowthSizeTable.ageFromBirthMillis(birthMillis, now)
            } else {
                member.age
            }
        } catch (_: Exception) {
            member.age
        }

        // Adults are not checked for outdated sizing
        if (ageYears >= 18) return 0

        val rec = GrowthSizeTable.getRecommendedSize(member.gender, ageYears)
        val items = dao.getItemsByMember(memberId)

        return items.count { item ->

            // Only size-relevant categories
            if (item.category !in listOf("TOP", "PANTS", "SHOES")) return@count false

            val sizeLabelField = ClothingItem::class.java.getDeclaredField("sizeLabel")
            sizeLabelField.isAccessible = true

            val sizeLabel = sizeLabelField.get(item) as? String
            val numericSize = sizeLabel
                ?.filter { it.isDigit() }
                ?.toIntOrNull()
                ?: return@count false

            // Compare with recommended size
            when (item.category) {
                "TOP"   -> rec.top   != null && numericSize < rec.top
                "PANTS" -> rec.pants != null && numericSize < rec.pants
                "SHOES" -> rec.shoes != null && numericSize < rec.shoes
                else    -> false
            }
        }
    }

    // ---------------------------------------------------------------------
    // Mark a group of items as “worn today”
    // ---------------------------------------------------------------------

    /**
     * Record that a set of items has been worn.
     * Only updates lastWornAt; does not modify any other fields.
     */
    suspend fun markItemsAsWorn(items: List<ClothingItem>) {
        val now = System.currentTimeMillis()
        items.forEach { item ->
            dao.upsertItem(item.copy(lastWornAt = now))
        }
    }

    // ---------------------------------------------------------------------
    // NFC Tag → Location Binding
    // ---------------------------------------------------------------------

    /**
     * Bind an NFC tag to a specific storage Location.
     *
     * If the tag already exists, its bound Location is replaced.
     * If the tag is new, it is created with the given locationId.
     *
     * NFC tag IDs come from Tag.id (byte array) converted to hex strings
     * such as "04AABB2299FF".
     */
    suspend fun bindNfcTagToLocation(tagId: String, locationId: Long) {
        dao.upsertNfcTag(NfcTagEntity(tagId = tagId, locationId = locationId))
    }

    /**
     * Look up the Location bound to a given NFC tag.
     *
     * Returns:
     *  - The Location object if the tag is known
     *  - null if the tag is not bound to any location
     */
    suspend fun getLocationForTag(tagId: String): Location? {
        val record = dao.getNfcTag(tagId) ?: return null
        return dao.getLocationById(record.locationId)
    }

    // ---------------------------------------------------------------------
    // NFC helpers: resolve items for a given location
    // ---------------------------------------------------------------------

    /**
     * Returns all clothing items stored under the given location.
     */
    suspend fun getItemsByLocation(locationId: Long): List<ClothingItem> {
        return dao.getItemsByLocation(locationId)
    }

    /**
     * Returns the Location row for the given id, or null if not found.
     */
    suspend fun getLocationById(locationId: Long): Location? {
        return dao.getLocationById(locationId)
    }

    // ---------------------------------------------------------------------
    // Statistics functions (used by StatisticsScreen)
    // ---------------------------------------------------------------------

    fun getCountByMember(): Flow<List<NameCount>> = dao.getCountByMember()

    fun getCountBySeason(): Flow<List<SeasonCount>> = dao.getCountBySeason()

    fun getCountByCategory(): Flow<List<CategoryCount>> = dao.getCountByCategory()
}