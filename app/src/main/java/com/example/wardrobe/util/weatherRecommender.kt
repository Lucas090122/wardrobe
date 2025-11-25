package com.example.wardrobe.util

import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.Season
import com.example.wardrobe.data.WeatherInfo
import kotlin.math.roundToInt

object WeatherRecommender {

    private const val RECENT_WINDOW_DAYS = 3
    private const val RECENT_WINDOW_MS =
        RECENT_WINDOW_DAYS * 24 * 60 * 60 * 1000L

    data class Outfit(
        val top: ClothingItem?,
        val pants: ClothingItem?,
        val shoes: ClothingItem?
    )

    /** UI will use this to select localized message */
    enum class ReasonCode {
        BASIC,
        AVOIDING_RECENT,
        NO_MATCH,
        MISSING_CATEGORY,
        NO_COMBINATIONS
    }

    data class Result(
        val outfit: Outfit?,
        val reasonCode: ReasonCode,
        val temperatureRounded: Int,
        val canRefresh: Boolean,
        val debugLog: String
    )

    fun recommend(
        weather: WeatherInfo,
        items: List<ClothingItem>,
        lastOutfit: Outfit? = null,
        now: Long = System.currentTimeMillis()
    ): Result {

        val tempRounded = weather.apparentTemperature.roundToInt()
        val debug = StringBuilder()

        val byWeather = filterByWeather(weather, items, debug)
        if (byWeather.isEmpty()) {
            return Result(
                outfit = null,
                reasonCode = ReasonCode.NO_MATCH,
                temperatureRounded = tempRounded,
                canRefresh = false,
                debugLog = debug.toString()
            )
        }

        val cutoff = now - RECENT_WINDOW_MS
        val notRecent = byWeather.filter { it.lastWornAt == 0L || it.lastWornAt < cutoff }
        val baseList = if (notRecent.isNotEmpty()) notRecent else byWeather

        val tops = baseList.filter { it.category == "TOP" }
        val pants = baseList.filter { it.category == "PANTS" }
        val shoes = baseList.filter { it.category == "SHOES" }

        if (tops.isEmpty() || pants.isEmpty() || shoes.isEmpty()) {
            return Result(
                outfit = null,
                reasonCode = ReasonCode.MISSING_CATEGORY,
                temperatureRounded = tempRounded,
                canRefresh = false,
                debugLog = debug.toString()
            )
        }

        // outfit combinations
        val all = mutableListOf<Outfit>()
        for (t in tops) for (p in pants) for (s in shoes) {
            all += Outfit(t, p, s)
        }
        if (all.isEmpty()) {
            return Result(
                outfit = null,
                reasonCode = ReasonCode.NO_COMBINATIONS,
                temperatureRounded = tempRounded,
                canRefresh = false,
                debugLog = debug.toString()
            )
        }

        // avoid same last outfit
        val candidates =
            if (lastOutfit == null) all
            else all.filter {
                it.top?.itemId != lastOutfit.top?.itemId ||
                        it.pants?.itemId != lastOutfit.pants?.itemId ||
                        it.shoes?.itemId != lastOutfit.shoes?.itemId
            }.ifEmpty { all }

        val outfit = candidates.random()
        val avoidingRecent = baseList === notRecent

        return Result(
            outfit = outfit,
            reasonCode = if (avoidingRecent) ReasonCode.AVOIDING_RECENT else ReasonCode.BASIC,
            temperatureRounded = tempRounded,
            canRefresh = candidates.size > 1,
            debugLog = debug.toString()
        )
    }

    private fun filterByWeather(
        weather: WeatherInfo,
        items: List<ClothingItem>,
        debug: StringBuilder
    ): List<ClothingItem> {

        val temp = weather.apparentTemperature.roundToInt()
        val rainy = weather.weatherCode in 51..67 || weather.weatherCode in 80..82

        val targetWarmth = when {
            temp <= -10 -> 5
            temp <= 0 -> 4
            temp <= 10 -> 3
            temp <= 18 -> 2
            else -> 1
        }

        val season = when {
            temp <= 5 -> Season.WINTER
            temp <= 18 -> Season.SPRING_AUTUMN
            else -> Season.SUMMER
        }

        var filtered = items.filter { it.warmthLevel >= targetWarmth - 1 }
        filtered = filtered.filter { it.season == season || it.season == Season.SPRING_AUTUMN }
        if (rainy) filtered = filtered.filter { it.isWaterproof }

        return filtered
    }
}
