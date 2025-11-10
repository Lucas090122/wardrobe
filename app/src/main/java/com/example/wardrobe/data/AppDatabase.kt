package com.example.wardrobe.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Member::class, ClothingItem::class, Tag::class, ClothingTagCrossRef::class, Location::class],
    version = 4,
    exportSchema = false
)
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
                    .fallbackToDestructiveMigration()
                    .build()
                    .also {
                        it.settingsRepository = SettingsRepository(context.applicationContext)
                    }
                INSTANCE = newInstance
                newInstance
            }
        }
    }
}