package com.example.wardrobe.util

import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.Season
import com.example.wardrobe.data.WeatherInfo
import kotlin.math.roundToInt

/**
 * Weather-based outfit recommender.
 *
 * Takes current weather + available clothing items and returns a recommended outfit.
 * Produces detailed debug logs to help understand filtering steps.
 */
object WeatherRecommender {

    /**
     * Time window used to avoid recommending items that were worn very recently.
     * Clothes worn within the past 3 days will be deprioritized.
     */
    private const val RECENT_WINDOW_DAYS = 3
    private const val RECENT_WINDOW_MS = RECENT_WINDOW_DAYS * 24 * 60 * 60 * 1000L

    /**
     * Represents a complete outfit combination.
     * All fields are nullable because filtering may remove some categories.
     */
    data class Outfit(
        val top: ClothingItem?,
        val pants: ClothingItem?,
        val shoes: ClothingItem?
    )

    /**
     * Full recommendation result.
     *
     * outfit     — The selected outfit (or null if none is possible)
     * reason     — Human-readable explanation for UI
     * canRefresh — Whether it's possible to pick the next different outfit
     * debugLog   — Detailed internal reasoning log
     */
    data class Result(
        val outfit: Outfit?,
        val reason: String,
        val canRefresh: Boolean,
        val debugLog: String
    )

    /**
     * Recommend an outfit based on weather and available clothing.
     *
     * Steps:
     * 1. Filter clothes by weather (warmth, rain, season)
     * 2. Filter out recently worn items (past 3 days)
     * 3. Categorize items into TOP / PANTS / SHOES
     * 4. Generate all outfit combinations
     * 5. Avoid repeating last outfit if possible
     * 6. Randomly pick one from remaining candidates
     */
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

        // ----------------------------------------
        // STEP 1: Weather-based filtering
        // ----------------------------------------
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

        // ----------------------------------------
        // STEP 2: Filter out recently-worn clothes
        // ----------------------------------------
        val notRecentlyWorn = byWeather.filter {
            it.lastWornAt == 0L || it.lastWornAt < cutoff
        }
        val baseList = if (notRecentlyWorn.isNotEmpty()) notRecentlyWorn else byWeather

        debug.append("After recent-worn filter: ${baseList.size}\n\n")

        // ----------------------------------------
        // STEP 3: Categorization (TOP, PANTS, SHOES)
        // ----------------------------------------
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

        // ----------------------------------------
        // STEP 4: Generate all possible outfits
        // ----------------------------------------
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

        // ----------------------------------------
        // STEP 5: Avoid repeating last outfit
        // ----------------------------------------
        val candidateOutfits =
            if (lastOutfit == null) allOutfits
            else allOutfits.filter {
                it.top?.itemId != lastOutfit.top?.itemId ||
                        it.pants?.itemId != lastOutfit.pants?.itemId ||
                        it.shoes?.itemId != lastOutfit.shoes?.itemId
            }.ifEmpty { allOutfits }

        debug.append("Candidate outfits: ${candidateOutfits.size}\n")

        // Randomly pick one from remaining candidates
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

    /**
     * Weather-based filtering logic:
     * - Warmth requirement based on temperature
     * - Seasonal filtering (Winter / Spring-Autumn / Summer)
     * - Rainy days → require waterproof items (except TOPs)
     */
    private fun filterByWeather(
        weather: WeatherInfo,
        items: List<ClothingItem>,
        debug: StringBuilder
    ): List<ClothingItem> {
        val temp = weather.apparentTemperature.roundToInt()
        val rainy = weather.weatherCode in 51..67 || weather.weatherCode in 80..82

        debug.append("[Weather Filter]\n")
        debug.append("rainy=$rainy\n")

        // Temperature → target warmth range
        val targetWarmth = when {
            temp <= -10 -> 5
            temp <= 0 -> 4
            temp <= 10 -> 3
            temp <= 18 -> 2
            else -> 1
        }
        debug.append("targetWarmth = $targetWarmth\n")

        // Determine season by temperature
        val season = when {
            temp <= 5 -> Season.WINTER
            temp <= 18 -> Season.SPRING_AUTUMN
            else -> Season.SUMMER
        }
        debug.append("season = $season\n")

        // Warmth-level filtering
        var filtered = items.filter { it.warmthLevel >= targetWarmth - 1 }
        debug.append("After warmth filter: ${filtered.size}\n")

        // Seasonal filtering (spring/autumn clothes are flexible)
        filtered = filtered.filter {
            it.season == season || it.season == Season.SPRING_AUTUMN
        }
        debug.append("After season filter: ${filtered.size}\n")

        // Rain filter: only waterproof items allowed (TOP is exempt from waterproof constraint)
        if (rainy) {
            debug.append("Rainy → filtering waterproof\n")
            filtered = filtered.filter { it.isWaterproof }
            debug.append("After waterproof filter: ${filtered.size}\n")
        }

        debug.append("\n")
        return filtered
    }
}