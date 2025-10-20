package com.example.wardrobe.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: ClothingItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTag(tag: Tag): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCrossRefs(refs: List<ClothingTagCrossRef>)

    @Query("DELETE FROM ClothingTagCrossRef WHERE itemId = :itemId")
    suspend fun clearTagsForItem(itemId: Long)

    @Transaction
    @Query(
        "SELECT * FROM ClothingItem WHERE (:q IS NULL OR :q = '' OR description LIKE '%' || :q || '%') ORDER BY createdAt DESC"
    )
    fun itemsStream(q: String?): Flow<List<ClothingItem>>

    @Transaction
    @Query(
        """
        SELECT ClothingItem.* FROM ClothingItem
        INNER JOIN ClothingTagCrossRef ON ClothingItem.itemId = ClothingTagCrossRef.itemId
        WHERE ClothingTagCrossRef.tagId IN (:tagIds)
          AND (:q IS NULL OR :q = '' OR ClothingItem.description LIKE '%' || :q || '%')
        GROUP BY ClothingItem.itemId
        ORDER BY ClothingItem.createdAt DESC
        """
    )
    fun itemsByTagsStream(tagIds: List<Long>, q: String?): Flow<List<ClothingItem>>

    @Transaction
    @Query("SELECT * FROM ClothingItem WHERE itemId = :itemId")
    fun itemWithTags(itemId: Long): Flow<ClothingWithTags?>

    @Query("SELECT * FROM Tag ORDER BY tagId ASC")
    fun allTags(): Flow<List<Tag>>

    @Query("SELECT name FROM Tag")
    suspend fun tagNamesOnce(): List<String>

    @Query("DELETE FROM ClothingItem WHERE itemId = :itemId")
    suspend fun deleteItemById(itemId: Long)

    @Query("SELECT createdAt FROM ClothingItem WHERE itemId = :itemId")
    suspend fun getCreatedAt(itemId: Long): Long?
}