package com.example.wardrobe.data

import androidx.room.TypeConverter

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
