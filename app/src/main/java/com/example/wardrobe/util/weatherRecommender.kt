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
        val canRefresh: Boolean,
        val debugLog: String        // ← Debug 输出
    )

    fun recommend(
        weather: WeatherInfo,
        items: List<ClothingItem>,
        lastOutfit: Outfit? = null,
        now: Long = System.currentTimeMillis()
    ): Result {

        val debug = StringBuilder()
        debug.append("---- Weather Debug ----\n")
        debug.append("Total items: ${items.size}\n")
        debug.append("Feel temp: ${weather.apparentTemperature}\n")
        debug.append("Wind: ${weather.windSpeed}\n")
        debug.append("WeatherCode: ${weather.weatherCode}\n\n")

        // Step 1: 经过天气过滤
        val byWeather = filterByWeather(weather, items, debug)

        if (byWeather.isEmpty()) {
            debug.append("No items after weather filtering.\n")
            return Result(
                outfit = null,
                reason = "No clothes match today's weather filters.",
                canRefresh = false,
                debugLog = debug.toString()
            )
        }

        val cutoff = now - RECENT_WINDOW_MS

        // Step 2: 最近穿过的过滤
        val notRecentlyWorn = byWeather.filter { it.lastWornAt == 0L || it.lastWornAt < cutoff }
        val baseList = if (notRecentlyWorn.isNotEmpty()) notRecentlyWorn else byWeather

        debug.append("After recent-worn filter: ${baseList.size}\n\n")

        // Step 3: 分类
        val tops = baseList.filter { it.category == "TOP" }
        val pants = baseList.filter { it.category == "PANTS" }
        val shoes = baseList.filter { it.category == "SHOES" }

        debug.append("TOP: ${tops.size}, PANTS: ${pants.size}, SHOES: ${shoes.size}\n")

        if (tops.isEmpty() || pants.isEmpty() || shoes.isEmpty()) {
            debug.append("Missing category → cannot form outfit.\n")
            return Result(
                outfit = null,
                reason = "Not enough clothing to form an outfit (need top, pants and shoes).",
                canRefresh = false,
                debugLog = debug.toString()
            )
        }

        // Step 4: 生成全部组合
        val allOutfits = mutableListOf<Outfit>()
        for (t in tops) for (p in pants) for (s in shoes) {
            allOutfits += Outfit(t, p, s)
        }
        debug.append("All outfit combinations: ${allOutfits.size}\n")

        if (allOutfits.isEmpty()) {
            debug.append("No valid outfit combinations.\n")
            return Result(
                outfit = null,
                reason = "No valid outfit combinations found.",
                canRefresh = false,
                debugLog = debug.toString()
            )
        }

        // Step 5: 避免重复穿搭
        val candidateOutfits =
            if (lastOutfit == null) allOutfits
            else allOutfits.filter {
                it.top?.itemId != lastOutfit.top?.itemId ||
                        it.pants?.itemId != lastOutfit.pants?.itemId ||
                        it.shoes?.itemId != lastOutfit.shoes?.itemId
            }.ifEmpty { allOutfits }

        debug.append("Candidate outfits: ${candidateOutfits.size}\n")

        val chosen = candidateOutfits.random()

        val reason = buildString {
            append("Feels like ${weather.apparentTemperature.roundToInt()}°C. ")
            if (baseList === notRecentlyWorn) append("Avoiding recently worn clothes. ")
        }.trim()

        return Result(
            outfit = chosen,
            reason = reason,
            canRefresh = candidateOutfits.size > 1,
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

        debug.append("[Weather Filter]\n")
        debug.append("rainy=$rainy\n")

        val targetWarmth = when {
            temp <= -10 -> 5
            temp <= 0 -> 4
            temp <= 10 -> 3
            temp <= 18 -> 2
            else -> 1
        }
        debug.append("targetWarmth = $targetWarmth\n")

        val season = when {
            temp <= 5 -> Season.WINTER
            temp <= 18 -> Season.SPRING_AUTUMN
            else -> Season.SUMMER
        }
        debug.append("season = $season\n")

        // Warmth filter
        var filtered = items.filter { it.warmthLevel >= targetWarmth - 1 }
        debug.append("After warmth filter: ${filtered.size}\n")

        // Season filter
        filtered = filtered.filter { it.season == season || it.season == Season.SPRING_AUTUMN }
        debug.append("After season filter: ${filtered.size}\n")

        // Rain filter（只过滤 waterproof，不动 TOP）
        if (rainy) {
            debug.append("Rainy → filtering waterproof\n")
            filtered = filtered.filter { it.isWaterproof }
            debug.append("After waterproof filter: ${filtered.size}\n")
        }

        debug.append("\n")
        return filtered
    }
}
