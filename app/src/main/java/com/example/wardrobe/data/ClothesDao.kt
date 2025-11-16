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

    @Query("SELECT COUNT(itemId) FROM ClothingItem WHERE locationId = :locationId")
    suspend fun getItemCountForLocation(locationId: Long): Int

    // Item operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: ClothingItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTag(tag: Tag): Long

    @Query("DELETE FROM Tag WHERE tagId = :tagId")
    suspend fun deleteTag(tagId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCrossRefs(refs: List<ClothingTagCrossRef>)

    @Query("DELETE FROM ClothingTagCrossRef WHERE itemId = :itemId")
    suspend fun clearTagsForItem(itemId: Long)

    @Transaction
    @Query(
        "SELECT * FROM ClothingItem WHERE ownerMemberId = :memberId AND (:q IS NULL OR :q = '' OR description LIKE '%' || :q || '%') AND (:season IS NULL OR season = :season) ORDER BY createdAt DESC"
    )
    fun itemsStream(memberId: Long, q: String?, season: String?): Flow<List<ClothingItem>>

    @Transaction
    @Query(
        """
        SELECT ClothingItem.* FROM ClothingItem
        INNER JOIN ClothingTagCrossRef ON ClothingItem.itemId = ClothingTagCrossRef.itemId
        WHERE ClothingItem.ownerMemberId = :memberId
          AND ClothingTagCrossRef.tagId IN (:tagIds)
          AND (:q IS NULL OR :q = '' OR ClothingItem.description LIKE '%' || :q || '%')
          AND (:season IS NULL OR season = :season)
        GROUP BY ClothingItem.itemId
        ORDER BY ClothingItem.createdAt DESC
        """
    )
    fun itemsByTagsStream(memberId: Long, tagIds: List<Long>, q: String?, season: String?): Flow<List<ClothingItem>>

    @Transaction
    @Query("SELECT * FROM ClothingItem WHERE itemId = :itemId")
    fun itemWithTags(itemId: Long): Flow<ClothingWithTags?>

    @Query("SELECT * FROM Tag ORDER BY tagId ASC")
    fun allTags(): Flow<List<Tag>>

    @Query("SELECT name FROM Tag")
    suspend fun tagNamesOnce(): List<String>

    @Query("SELECT * FROM Tag WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    @Query("SELECT COUNT(itemId) FROM ClothingTagCrossRef WHERE tagId = :tagId")
    suspend fun getItemCountForTag(tagId: Long): Int

    @Query("""
        SELECT t.tagId, t.name, COUNT(ci.itemId) as count
        FROM Tag t
        LEFT JOIN ClothingTagCrossRef ctc ON t.tagId = ctc.tagId
        LEFT JOIN ClothingItem ci ON ctc.itemId = ci.itemId AND ci.ownerMemberId = :memberId AND ci.stored = :isStored
        GROUP BY t.tagId, t.name
        ORDER BY t.name ASC
    """)
    fun getTagsWithCounts(memberId: Long, isStored: Boolean): Flow<List<TagWithCount>>

    @Query("DELETE FROM ClothingItem WHERE itemId = :itemId")
    suspend fun deleteItemById(itemId: Long)

    @Query("SELECT createdAt FROM ClothingItem WHERE itemId = :itemId")
    suspend fun getCreatedAt(itemId: Long): Long?

    @Query("UPDATE ClothingItem SET ownerMemberId = :newOwnerMemberId WHERE itemId = :itemId")
    suspend fun updateItemOwner(itemId: Long, newOwnerMemberId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransferHistory(transferHistory: TransferHistory)

    @Transaction
    @Query("""
        SELECT
            TH.transferTime,
            SM.name AS sourceMemberName,
            TM.name AS targetMemberName,
            CI.description AS itemName
        FROM TransferHistory AS TH
        INNER JOIN Member AS SM ON TH.sourceMemberId = SM.memberId
        INNER JOIN Member AS TM ON TH.targetMemberId = TM.memberId
        INNER JOIN ClothingItem AS CI ON TH.itemId = CI.itemId
        ORDER BY TH.transferTime DESC
    """)
    fun getAllTransferHistoryDetails(): Flow<List<TransferHistoryDetails>>
}