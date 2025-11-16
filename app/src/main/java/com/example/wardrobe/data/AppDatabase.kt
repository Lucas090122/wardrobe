package com.example.wardrobe.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Member::class, ClothingItem::class, Tag::class, ClothingTagCrossRef::class, Location::class, TransferHistory::class],
    version = 7, // Incremented version
    exportSchema = false
)
@TypeConverters(SeasonConverter::class) // Add TypeConverter
abstract class AppDatabase : RoomDatabase() {
    abstract fun clothesDao(): ClothesDao

    // This will be initialized in the companion object
    lateinit var settingsRepository: SettingsRepository
        private set

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE
                if (instance != null) {
                    return instance
                }

                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wardrobe.db"
                )
                    .addMigrations(MIGRATION_6_7) // Add migration
                    .build()
                    .also {
                        it.settingsRepository = SettingsRepository(context.applicationContext)
                    }
                INSTANCE = newInstance
                newInstance
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add the new 'season' column to the ClothingItem table
                db.execSQL("ALTER TABLE ClothingItem ADD COLUMN season TEXT NOT NULL DEFAULT 'SPRING_AUTUMN'")

                // 2. Create a temporary mapping of season name to items
                val seasonTagMapping = mapOf(
                    "Summer" to "SUMMER",
                    "Winter" to "WINTER",
                    "Spring/Autumn" to "SPRING_AUTUMN"
                )

                for ((tagName, seasonValue) in seasonTagMapping) {
                    // Find tagId for the season tag
                    val tagCursor = db.query("SELECT tagId FROM Tag WHERE name = ?", arrayOf(tagName))
                    if (tagCursor.moveToFirst()) {
                        val tagId = tagCursor.getLong(0)
                        tagCursor.close()

                        // Find all itemIds with this tag
                        val itemCursor = db.query("SELECT itemId FROM ClothingTagCrossRef WHERE tagId = ?", arrayOf(tagId))
                        val itemIds = mutableListOf<Long>()
                        while (itemCursor.moveToNext()) {
                            itemIds.add(itemCursor.getLong(0))
                        }
                        itemCursor.close()

                        // Update season for these items
                        if (itemIds.isNotEmpty()) {
                            val idsString = itemIds.joinToString(",")
                            db.execSQL("UPDATE ClothingItem SET season = '$seasonValue' WHERE itemId IN ($idsString)")
                        }

                        // Delete from ClothingTagCrossRef and Tag
                        db.execSQL("DELETE FROM ClothingTagCrossRef WHERE tagId = $tagId")
                        db.execSQL("DELETE FROM Tag WHERE tagId = $tagId")
                    } else {
                        tagCursor.close()
                    }
                }
            }
        }
    }
}