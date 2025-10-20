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

@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true) val tagId: Long = 0,
    @ColumnInfo(index = true) val name: String
)

@Entity
data class ClothingItem(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val description: String,
    val imageUri: String?,
    val createdAt: Long = System.currentTimeMillis()
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