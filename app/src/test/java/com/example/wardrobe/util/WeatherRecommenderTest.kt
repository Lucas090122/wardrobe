package com.example.wardrobe.util

import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.Season
import com.example.wardrobe.data.WeatherInfo // Use the correct WeatherInfo class
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.concurrent.TimeUnit

class WeatherRecommenderTest {

    private fun createTestItem(
        itemId: Long,
        category: String,
        warmthLevel: Int,
        season: Season,
        isWaterproof: Boolean = false,
        lastWornAt: Long = 0L
    ): ClothingItem {
        return ClothingItem(
            itemId = itemId,
            ownerMemberId = 1L,
            description = "Test $category",
            imageUri = null,
            category = category,
            warmthLevel = warmthLevel,
            occasions = "CASUAL",
            isWaterproof = isWaterproof,
            color = "#FFFFFF",
            lastWornAt = lastWornAt,
            isFavorite = false,
            season = season
        )
    }

    private val testTop = createTestItem(1, "TOP", 3, Season.SPRING_AUTUMN)
    private val testPants = createTestItem(2, "PANTS", 3, Season.SPRING_AUTUMN)
    private val testShoes = createTestItem(3, "SHOES", 3, Season.SPRING_AUTUMN)

    private val testItems = listOf(testTop, testPants, testShoes)

    // Helper to create WeatherInfo object
    private fun createWeather(feelsLike: Double, weatherCode: Int = 0) = WeatherInfo(
        temperature = 0.0,
        apparentTemperature = feelsLike,
        windSpeed = 0.0,
        uvIndex = 0.0,
        weatherCode = weatherCode,
        icon = ""
    )


    /**
     * EN: Tests that the recommender prefers items that were worn less recently.
     * CN: 测试推荐器是否会优先选择最近没有穿过的衣物。
     */
    @Test
    fun recommend_lastWornAt_prefersOlderItems() {
        val now = TimeUnit.DAYS.toMillis(10)
        val oneDayAgo = now - TimeUnit.DAYS.toMillis(1)
        val fourDaysAgo = now - TimeUnit.DAYS.toMillis(4)

        val recentTop = testTop.copy(itemId = 10, lastWornAt = oneDayAgo)
        val oldTop = testTop.copy(itemId = 11, lastWornAt = fourDaysAgo)

        val items = listOf(recentTop, oldTop, testPants, testShoes)
        val weather = createWeather(10.0)

        val result = WeatherRecommender.recommend(weather, items, now = now)

        assertThat(result.outfit).isNotNull()
        assertThat(result.outfit?.top).isEqualTo(oldTop)
        assertThat(result.reasonCode).isEqualTo(ReasonCode.AVOIDING_RECENT)
    }

    /**
     * EN: Tests that the recommender falls back to any item if all suitable items have been worn recently.
     * CN: 测试当所有合适的衣物都最近穿过时，推荐器是否能够回退并推荐其中一件。
     */
    @Test
    fun recommend_lastWornAt_fallsBackWhenAllRecent() {
        val now = TimeUnit.DAYS.toMillis(10)
        val oneDayAgo = now - TimeUnit.DAYS.toMillis(1)

        val recentTop = testTop.copy(lastWornAt = oneDayAgo)
        val recentPants = testPants.copy(lastWornAt = oneDayAgo)
        val recentShoes = testShoes.copy(lastWornAt = oneDayAgo)
        val items = listOf(recentTop, recentPants, recentShoes)
        val weather = createWeather(10.0)

        val result = WeatherRecommender.recommend(weather, items, now = now)

        assertThat(result.outfit).isNotNull()
        assertThat(result.reasonCode).isEqualTo(ReasonCode.BASIC)
    }

    /**
     * EN: Tests that the recommendation fails if a required clothing category (e.g., pants) is missing.
     * CN: 测试当缺少必要的衣物类别（例如，裤子）时，推荐是否会失败。
     */
    @Test
    fun recommend_missingCategory_fails() {
        val items = listOf(testTop, testShoes)
        val weather = createWeather(10.0)

        val result = WeatherRecommender.recommend(weather, items)

        assertThat(result.outfit).isNull()
        assertThat(result.reasonCode).isEqualTo(ReasonCode.MISSING_CATEGORY)
    }

    /**
     * EN: Tests that the recommendation fails when no items match the current weather conditions.
     * CN: 测试当没有衣物匹配当前天气状况时，推荐是否会失败。
     */
    @Test
    fun recommend_noMatch_fails() {
        val summerItems = listOf(testTop.copy(season = Season.SUMMER), testPants.copy(season = Season.SUMMER), testShoes.copy(season = Season.SUMMER))
        val weather = createWeather(-5.0)

        val result = WeatherRecommender.recommend(weather, summerItems)

        assertThat(result.outfit).isNull()
        assertThat(result.reasonCode).isEqualTo(ReasonCode.NO_MATCH)
    }

    /**
     * EN: Tests that the recommender prefers waterproof items during rainy weather.
     * CN: 测试在雨天时，推荐器是否会优先选择防水的衣物。
     */
    @Test
    fun recommend_rainy_prefersWaterproof() {
        val waterproofShoes = createTestItem(30, "SHOES", 3, Season.SPRING_AUTUMN, isWaterproof = true)
        val waterproofTop = createTestItem(31, "TOP", 3, Season.SPRING_AUTUMN, isWaterproof = true)
        val waterproofPants = createTestItem(32, "PANTS", 3, Season.SPRING_AUTUMN, isWaterproof = true)
        val nonWaterproofShoes = createTestItem(33, "SHOES", 3, Season.SPRING_AUTUMN, isWaterproof = false)

        val items = listOf(waterproofTop, waterproofPants, waterproofShoes, nonWaterproofShoes)
        val weather = createWeather(10.0, weatherCode = 61) // Rainy weather code

        val result = WeatherRecommender.recommend(weather, items)

        assertThat(result.outfit).isNotNull()
        assertThat(result.outfit?.shoes?.isWaterproof).isTrue()
        assertThat(result.outfit?.shoes).isEqualTo(waterproofShoes)
    }

    /**
     * EN: Tests that the `canRefresh` flag is true when there are multiple possible outfit combinations.
     * CN: 测试当存在多种穿搭组合时，`canRefresh` 标志是否为 true。
     */
    @Test
    fun recommend_canRefresh_isTrueForMultipleOutfits() {
        val anotherTop = testTop.copy(itemId = 20)
        val items = listOf(testTop, anotherTop, testPants, testShoes)
        val weather = createWeather(10.0)

        val result = WeatherRecommender.recommend(weather, items)

        assertThat(result.outfit).isNotNull()
        assertThat(result.canRefresh).isTrue()
    }

    /**
     * EN: Tests that requesting a new recommendation with the last outfit provided usually returns a different outfit.
     * CN: 测试当提供上一套穿搭并请求新的推荐时，通常会返回一套不同的穿搭。
     */
    @Test
    fun recommend_withLastOutfit_returnsDifferentOutfit() {
        val anotherTop = testTop.copy(itemId = 20)
        val items = listOf(testTop, anotherTop, testPants, testShoes)
        val weather = createWeather(10.0)

        val result1 = WeatherRecommender.recommend(weather, items)
        val firstOutfit = result1.outfit
        assertThat(firstOutfit).isNotNull()

        // The recommender might randomly pick the same outfit. Let's try a few times
        // to make the test more robust against this randomness.
        var result2 = result1
        for (i in 1..5) {
            result2 = WeatherRecommender.recommend(weather, items, lastOutfit = firstOutfit)
            if (result2.outfit != firstOutfit) {
                break // Found a different outfit
            }
        }

        assertThat(result2.outfit).isNotNull()
        assertThat(result2.outfit).isNotEqualTo(firstOutfit)
    }
}
