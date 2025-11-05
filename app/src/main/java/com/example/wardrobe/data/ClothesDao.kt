package com.example.wardrobe.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothesDao {
    // Member operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Query("SELECT * FROM Member ORDER BY name ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM Member WHERE memberId = :memberId")
    fun getMember(memberId: Long): Flow<Member?>

    // Location operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location): Long

    @Query("SELECT * FROM Location ORDER BY name ASC")
    fun getAllLocations(): Flow<List<Location>>

    @Query("DELETE FROM Location WHERE locationId = :locationId")
    suspend fun deleteLocation(locationId: Long)

    // Item operations
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
        "SELECT * FROM ClothingItem WHERE ownerMemberId = :memberId AND (:q IS NULL OR :q = '' OR description LIKE '%' || :q || '%') ORDER BY createdAt DESC"
    )
    fun itemsStream(memberId: Long, q: String?): Flow<List<ClothingItem>>

    @Transaction
    @Query(
        """
        SELECT ClothingItem.* FROM ClothingItem
        INNER JOIN ClothingTagCrossRef ON ClothingItem.itemId = ClothingTagCrossRef.itemId
        WHERE ClothingItem.ownerMemberId = :memberId
          AND ClothingTagCrossRef.tagId IN (:tagIds)
          AND (:q IS NULL OR :q = '' OR ClothingItem.description LIKE '%' || :q || '%')
        GROUP BY ClothingItem.itemId
        ORDER BY ClothingItem.createdAt DESC
        """
    )
    fun itemsByTagsStream(memberId: Long, tagIds: List<Long>, q: String?): Flow<List<ClothingItem>>

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