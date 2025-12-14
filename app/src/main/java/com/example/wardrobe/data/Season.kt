package com.example.wardrobe.data

import androidx.room.TypeConverter

/**
 * Season & SeasonConverter
 *
 * This file defines the Season enum used to represent clothing seasons
 * (Spring/Autumn, Summer, Winter) and a corresponding Room TypeConverter.
 *
 * Purpose:
 * - Provide a type-safe way to model season information in the domain layer
 * - Allow Room to persist the Season enum as a String in the database
 *
 * Design notes:
 * - The enum is stored using its name to keep the database human-readable
 * - A custom TypeConverter is required because Room cannot store enums directly
 * - Using String instead of ordinal avoids issues when enum order changes
 */
enum class Season {
    SPRING_AUTUMN,
    SUMMER,
    WINTER
}

class SeasonConverter {
    @TypeConverter
    fun fromSeason(season: Season): String = season.name

    @TypeConverter
    fun toSeason(name: String): Season = Season.valueOf(name)
}
