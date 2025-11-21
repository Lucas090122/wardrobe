package com.example.wardrobe.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.ForeignKey.Companion.SET_NULL

/**
 * Represents a family member / wardrobe owner.
 * Each ClothingItem is linked to one Member via ownerMemberId.
 */
@Entity
data class Member(
    @PrimaryKey(autoGenerate = true) val memberId: Long = 0,
    @ColumnInfo(index = true) val name: String,
    val gender: String,
    val age: Int,
    val birthDate: Long? = null
)

/**
 * Represents a global tag such as "Top", "Winter", "Sports".
 */
@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true) val tagId: Long = 0,
    @ColumnInfo(index = true) val name: String
)

/**
 * Represents a physical storage location, e.g., “Main Wardrobe”, “Box A”.
 */
@Entity
data class Location(
    @PrimaryKey(autoGenerate = true) val locationId: Long = 0,
    @ColumnInfo(index = true) val name: String
)

/**
 * Represents a clothing item owned by a given Member.
 *
 * Foreign keys:
 * - ownerMemberId → Member.memberId (CASCADE delete: deleting a Member deletes their items)
 * - locationId → Location.locationId (SET_NULL: location can be removed without deleting items)
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["memberId"],
            childColumns = ["ownerMemberId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Location::class,
            parentColumns = ["locationId"],
            childColumns = ["locationId"],
            onDelete = SET_NULL
        )
    ],
    indices = [Index(value = ["ownerMemberId"]), Index(value = ["locationId"])]
)
data class ClothingItem(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val ownerMemberId: Long,
    val description: String,
    val imageUri: String?,
    val stored: Boolean = false,
    val locationId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),

    // --- V2.4 added fields (AI-powered recommendation features) ---

    // Core recommended attributes
    @ColumnInfo(defaultValue = "'TOP'")
    val category: String,

    @ColumnInfo(defaultValue = "3")
    val warmthLevel: Int,

    /**
     * Comma-separated string (e.g., "CASUAL,WORK").
     * Room does not support storing lists directly, so we use a single text field.
     */
    @ColumnInfo(defaultValue = "'CASUAL'")
    val occasions: String,

    /**
     * Boolean stored as 0/1 in SQLite.
     */
    @ColumnInfo(defaultValue = "0")
    val isWaterproof: Boolean,

    // Additional optimized fields
    @ColumnInfo(defaultValue = "'#FFFFFF'")
    val color: String,

    /**
     * Timestamp of last time this clothing item was marked as worn.
     */
    @ColumnInfo(defaultValue = "0")
    val lastWornAt: Long,

    @ColumnInfo(defaultValue = "0")
    val isFavorite: Boolean,

    @ColumnInfo(defaultValue = "'SPRING_AUTUMN'")
    val season: Season,

    /**
     * Optional size label: e.g., "110", "28", "32"
     */
    val sizeLabel: String? = null
)

/**
 * Many-to-many relationship between ClothingItem and Tag.
 *
 * Primary key is the pair (itemId, tagId).
 * CASCADE ensures deleting an Item or Tag cleans up cross-ref rows.
 */
@Entity(
    primaryKeys = ["itemId", "tagId"],
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["tagId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ClothingItem::class,
            parentColumns = ["itemId"],
            childColumns = ["itemId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["tagId"],
            childColumns = ["tagId"],
            onDelete = CASCADE
        )
    ]
)
data class ClothingTagCrossRef(
    val itemId: Long,
    val tagId: Long
)

/**
 * Clothing item along with all associated tags.
 *
 * Uses:
 * - @Embedded for ClothingItem fields
 * - @Relation + Junction to build ClothingItem ↔ Tag many-to-many relationship
 */
data class ClothingWithTags(
    @Embedded val item: ClothingItem,
    @Relation(
        parentColumn = "itemId",
        entityColumn = "tagId",
        associateBy = Junction(ClothingTagCrossRef::class)
    )
    val tags: List<Tag>
)

/**
 * Query result for a Tag along with the number of items using it.
 */
data class TagWithCount(
    val tagId: Long,
    val name: String,
    val count: Int
)

/**
 * Records history of item transfers between members.
 *
 * Foreign keys:
 * - itemId → ClothingItem
 * - sourceMemberId → Member
 * - targetMemberId → Member
 *
 * CASCADE ensures transfer logs are removed when related objects are deleted.
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ClothingItem::class,
            parentColumns = ["itemId"],
            childColumns = ["itemId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["memberId"],
            childColumns = ["sourceMemberId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["memberId"],
            childColumns = ["targetMemberId"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["sourceMemberId"]),
        Index(value = ["targetMemberId"])
    ]
)
data class TransferHistory(
    @PrimaryKey(autoGenerate = true) val transferId: Long = 0,
    val itemId: Long,
    val sourceMemberId: Long,
    val targetMemberId: Long,
    val transferTime: Long = System.currentTimeMillis()
)

/**
 * Full transfer record containing names and item description,
 * typically used for UI display.
 */
data class TransferHistoryDetails(
    val transferTime: Long,
    val sourceMemberName: String,
    val targetMemberName: String,
    val itemName: String
)

/** Simple aggregated-count models used in statistics screens. */
data class NameCount(val name: String, val count: Int)
data class SeasonCount(val season: Season, val count: Int)
data class CategoryCount(val category: String, val count: Int)

/**
 * NFC tag binding to a specific physical storage location.
 *
 * Each NFC tag has a unique hardware ID (converted to a hex string).
 * Users can bind a tag to a Location inside Settings.
 *
 * When a Location is deleted, all bound tags are removed (CASCADE).
*/
@Entity(
    tableName = "NfcTag",
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["locationId"],
            childColumns = ["locationId"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index(value = ["locationId"])
    ]
)
data class NfcTagEntity(
    @PrimaryKey
    val tagId: String,       // NFC unique ID in hex format, e.g., "04AABB22FF01"
    val locationId: Long     // Bound storage location
)