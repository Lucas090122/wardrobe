package com.example.wardrobe.data

class WardrobeRepository(private val dao: ClothesDao) {
    fun observeTags() = dao.allTags()

    fun observeItems(selectedTagIds: List<Long>, query: String?) =
        if (selectedTagIds.isEmpty()) dao.itemsStream(query)
        else dao.itemsByTagsStream(selectedTagIds, query)

    fun observeItem(itemId: Long) = dao.itemWithTags(itemId)

    suspend fun saveItem(
        itemId: Long?,
        description: String,
        imageUri: String?,
        tagIds: List<Long>
    ): Long {
        val createdAt = if (itemId != null && itemId != 0L)
            dao.getCreatedAt(itemId) ?: System.currentTimeMillis()
        else
            System.currentTimeMillis()

        val id = dao.upsertItem(
            ClothingItem(itemId ?: 0, description, imageUri, createdAt)
        ).let { if (itemId != null && itemId != 0L) itemId else it }

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
}