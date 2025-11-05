package com.example.wardrobe.data

import kotlinx.coroutines.flow.Flow

class WardrobeRepository(private val dao: ClothesDao) {
    // Member functions
    fun getAllMembers(): Flow<List<Member>> = dao.getAllMembers()

    fun getMember(memberId: Long): Flow<Member?> = dao.getMember(memberId)

    suspend fun createMember(name: String, gender: String, age: Int): Long {
        return dao.insertMember(Member(name = name, gender = gender, age = age))
    }

    // Tag functions
    fun observeTags() = dao.allTags()

    fun observeTagsWithCounts(memberId: Long) = dao.getTagsWithCounts(memberId)

    // Item functions
    fun observeItems(memberId: Long, selectedTagIds: List<Long>, query: String?) =
        if (selectedTagIds.isEmpty()) dao.itemsStream(memberId, query)
        else dao.itemsByTagsStream(memberId, selectedTagIds, query)

    fun observeItem(itemId: Long) = dao.itemWithTags(itemId)

    suspend fun saveItem(
        memberId: Long, // Required owner
        itemId: Long?,
        description: String,
        imageUri: String?,
        tagIds: List<Long>
    ): Long {
        val createdAt = if (itemId != null && itemId != 0L)
            dao.getCreatedAt(itemId) ?: System.currentTimeMillis()
        else
            System.currentTimeMillis()

        val newClothingItem = ClothingItem(
            itemId = itemId ?: 0,
            ownerMemberId = memberId,
            description = description,
            imageUri = imageUri,
            createdAt = createdAt
        )

        val id = dao.upsertItem(newClothingItem).let { if (itemId != null && itemId != 0L) itemId else it }

        dao.clearTagsForItem(id)
        dao.upsertCrossRefs(tagIds.map { ClothingTagCrossRef(id, it) })
        return id
    }

    suspend fun ensureDefaultTags(names: List<String>) {
        val existing = dao.tagNamesOnce().toSet()
        names.filter { it !in existing }
            .forEach { dao.upsertTag(Tag(name = it)) }
    }

    suspend fun getOrCreateTag(name: String): Long {
        val sanitizedName = name.trim()
        if (sanitizedName.isEmpty()) return 0L

        val existing = dao.getTagByName(sanitizedName)
        return existing?.tagId ?: dao.upsertTag(Tag(name = sanitizedName))
    }

    suspend fun deleteItem(itemId: Long) {
        dao.clearTagsForItem(itemId)
        dao.deleteItemById(itemId)
    }
}