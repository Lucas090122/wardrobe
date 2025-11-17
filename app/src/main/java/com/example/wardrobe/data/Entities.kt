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

@Entity
data class Member(
    @PrimaryKey(autoGenerate = true) val memberId: Long = 0,
    @ColumnInfo(index = true) val name: String,
    val gender: String,
    val age: Int
)

@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true) val tagId: Long = 0,
    @ColumnInfo(index = true) val name: String
)

@Entity
data class Location(
    @PrimaryKey(autoGenerate = true) val locationId: Long = 0,
    @ColumnInfo(index = true) val name: String
)

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

    // --- V2.4 新增字段 ---

    // 核心推荐字段
    @ColumnInfo(defaultValue = "'TOP'")
    val category: String,

    @ColumnInfo(defaultValue = "3")
    val warmthLevel: Int,
    
    // 存储逗号分隔的字符串，如 "CASUAL,WORK"
    @ColumnInfo(defaultValue = "'CASUAL'") 
    val occasions: String,

    @ColumnInfo(defaultValue = "0") // Room中Boolean以0/1存储
    val isWaterproof: Boolean,

    // 优化字段
    @ColumnInfo(defaultValue = "'#FFFFFF'")
    val color: String,

    @ColumnInfo(defaultValue = "0")
    val lastWornAt: Long,

    @ColumnInfo(defaultValue = "0")
    val isFavorite: Boolean,

    @ColumnInfo(defaultValue = "'SPRING_AUTUMN'")
    val season: Season
)

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

data class ClothingWithTags(
    @Embedded val item: ClothingItem,
    @Relation(
        parentColumn = "itemId",
        entityColumn = "tagId",
        associateBy = Junction(ClothingTagCrossRef::class)
    )
    val tags: List<Tag>
)

data class TagWithCount(
    val tagId: Long,
    val name: String,
    val count: Int
)

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

data class TransferHistoryDetails(
    val transferTime: Long,
    val sourceMemberName: String,
    val targetMemberName: String,
    val itemName: String
)

data class NameCount(val name: String, val count: Int)
data class SeasonCount(val season: Season, val count: Int)
data class CategoryCount(val category: String, val count: Int)
