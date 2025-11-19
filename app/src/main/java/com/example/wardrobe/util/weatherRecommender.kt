//package com.example.wardrobe.ui.util
//
//import com.example.wardrobe.data.ClothingItem
//import com.example.wardrobe.data.Season
//import com.example.wardrobe.data.WeatherInfo
//import kotlin.math.roundToInt
//
//object WeatherRecommender {
//
//    data class RecommendationResult(
//        val recommended: List<ClothingItem>,
//        val reason: String
//    )
//
//    fun recommend(weather: WeatherInfo, items: List<ClothingItem>): RecommendationResult {
//        val feel = weather.apparentTemperature.roundToInt()
//        val windy = weather.windSpeed > 7
//        val rainy = when (weather.weatherCode) {
//            in 51..67, in 80..82 -> true
//            else -> false
//        }
//
//        val targetWarmth = when {
//            feel <= -10 -> 5
//            feel <= 0 -> 4
//            feel <= 10 -> 3
//            feel <= 18 -> 2
//            else -> 1
//        }
//
//        val season = when {
//            feel <= 5 -> Season.WINTER
//            feel <= 18 -> Season.SPRING_AUTUMN
//            else -> Season.SUMMER
//        }
//
//        var filtered = items.filter { it.warmthLevel >= targetWarmth - 1 }
//
//        filtered = filtered.filter {
//            it.season == season || it.season == Season.SPRING_AUTUMN
//        }
//
//        if (rainy) {
//            filtered = filtered.filter { it.isWaterproof }
//        }
//
//        if (windy) {
//            filtered = filtered.filter { it.category != "TOP" }
//        }
//
//        filtered = filtered.sortedByDescending { it.isFavorite }
//
//        val reason = buildString {
//            append("Feels like ${feel}°, target warmth $targetWarmth. ")
//            if (rainy) append("Rain detected. ")
//            if (windy) append("Strong wind. ")
//        }.trim()
//
//        return RecommendationResult(filtered.take(5), reason)
//    }
//}
//1-------------
package com.example.wardrobe.ui.util

import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.Season
import com.example.wardrobe.data.WeatherInfo
import kotlin.math.roundToInt

object WeatherRecommender {

    private const val RECENT_WINDOW_DAYS = 3
    private const val RECENT_WINDOW_MS = RECENT_WINDOW_DAYS * 24 * 60 * 60 * 1000L

    data class Outfit(
        val top: ClothingItem?,
        val pants: ClothingItem?,
        val shoes: ClothingItem?
    )

    data class Result(
        val outfit: Outfit?,
        val reason: String,
        val canRefresh: Boolean
    )

    fun recommend(
        weather: WeatherInfo,
        items: List<ClothingItem>,
        lastOutfit: Outfit? = null,
        now: Long = System.currentTimeMillis()
    ): Result {

        val byWeather = filterByWeather(weather, items)

        if (byWeather.isEmpty()) {
            return Result(
                outfit = null,
                reason = "No clothes match today's weather filters.",
                canRefresh = false
            )
        }

        val cutoff = now - RECENT_WINDOW_MS

        // 优先使用最近几天没有穿过的衣服
        val notRecentlyWorn = byWeather.filter { it.lastWornAt == 0L || it.lastWornAt < cutoff }
        val baseList = if (notRecentlyWorn.isNotEmpty()) notRecentlyWorn else byWeather

        val tops = baseList.filter { it.category == "TOP" }
        val pants = baseList.filter { it.category == "PANTS" }
        val shoes = baseList.filter { it.category == "SHOES" }

        if (tops.isEmpty() || pants.isEmpty() || shoes.isEmpty()) {
            return Result(
                outfit = null,
                reason = "Not enough clothing to form an outfit (need top, pants and shoes).",
                canRefresh = false
            )
        }

        // 生成所有组合
        val allOutfits = mutableListOf<Outfit>()
        for (t in tops) {
            for (p in pants) {
                for (s in shoes) {
                    allOutfits += Outfit(t, p, s)
                }
            }
        }

        if (allOutfits.isEmpty()) {
            return Result(
                outfit = null,
                reason = "No valid outfit combinations found.",
                canRefresh = false
            )
        }

        // 尝试避免和上一套完全相同
        val candidateOutfits =
            if (lastOutfit == null) allOutfits
            else allOutfits.filter {
                it.top?.itemId != lastOutfit.top?.itemId ||
                        it.pants?.itemId != lastOutfit.pants?.itemId ||
                        it.shoes?.itemId != lastOutfit.shoes?.itemId
            }.ifEmpty { allOutfits }

        val chosen = candidateOutfits.random()

        val reason = buildString {
            append("Feels like ${weather.apparentTemperature.roundToInt()}°C. ")
            if (baseList === notRecentlyWorn) {
                append("Avoiding clothes worn in last $RECENT_WINDOW_DAYS days. ")
            } else {
                append("All suitable clothes were worn recently, suggesting anyway. ")
            }
        }.trim()

        val canRefresh = candidateOutfits.size > 1

        return Result(
            outfit = chosen,
            reason = reason,
            canRefresh = canRefresh
        )
    }

    private fun filterByWeather(
        weather: WeatherInfo,
        items: List<ClothingItem>
    ): List<ClothingItem> {
        val temp = weather.apparentTemperature.roundToInt()
        val windy = weather.windSpeed > 7
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

        filtered = filtered.filter {
            it.season == season || it.season == Season.SPRING_AUTUMN
        }

        if (rainy) {
            filtered = filtered.filter { it.isWaterproof }
        }

        if (windy) {
            filtered = filtered.filter { it.category != "TOP" }
        }

        // favorite 优先
        return filtered.sortedByDescending { it.isFavorite }
    }
}
