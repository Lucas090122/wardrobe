package com.example.wardrobe.data

import kotlin.math.max

/**
 * A simple (example) mapping table from age → recommended clothing sizes.
 * You can update these values later according to real growth charts.
 *
 * Logic overview:
 *  - For each age (1–18), we store an average height.
 *  - Clothing sizes are derived from height using simple formulas.
 *  - Shoe size is estimated based on a proportional foot-length model.
 */
object GrowthSizeTable {

    /**
     * Recommended size set for one age.
     *
     * @param top    Recommended top size (e.g. 110, 120)
     * @param pants  Recommended pants size (same rule as top)
     * @param shoes  Recommended shoe size (EU sizing)
     */
    data class RecommendedSizes(
        val top: Int?,
        val pants: Int?,
        val shoes: Int?
    )

    /**
     * Example height table for boys (in cm).
     * Values approximate average growth curves.
     */
    private val boyHeight = mapOf(
        1 to 76.5,  2 to 88.5,  3 to 96.8,  4 to 104.1,
        5 to 111.3, 6 to 117.7, 7 to 124.0, 8 to 130.0,
        9 to 135.4, 10 to 140.2, 11 to 145.3, 12 to 151.9,
        13 to 159.5, 14 to 165.9, 15 to 169.8, 16 to 171.6,
        17 to 172.3, 18 to 172.7
    )

    /**
     * Example height table for girls (in cm).
     */
    private val girlHeight = mapOf(
        1 to 75.0,  2 to 87.2,  3 to 95.6,  4 to 103.1,
        5 to 110.2, 6 to 116.6, 7 to 122.5, 8 to 128.5,
        9 to 134.1, 10 to 140.1, 11 to 146.6, 12 to 152.4,
        13 to 156.3, 14 to 158.6, 15 to 169.8, 16 to 160.1,
        17 to 160.3, 18 to 160.9
    )

    /**
     * Convert height (cm) to clothing size (top/pants).
     * Rule: round height to the nearest ten and use it directly (e.g., 121 → 120).
     */
    private fun heightToTopPantSize(height: Double): Int {
        return (Math.round(height / 10) * 10).toInt()
    }

    /**
     * Convert height (cm) to shoe EU size.
     *
     * Approximation:
     *   footLength = height * 0.155
     *   EU size = footLength * 1.5 + 1
     *
     * These constants are illustrative and can be refined.
     */
    private fun heightToShoeEU(height: Double): Int {
        val footLength = height * 0.155
        val eu = footLength * 1.5 + 1
        return eu.toInt()
    }

    /**
     * Get recommended sizes for a given gender and age.
     *
     * @return null values if the gender is unknown or age out of table range.
     */
    fun getRecommendedSize(gender: String, age: Int): RecommendedSizes {
        val height = when (gender.lowercase()) {
            "boy", "male", "m", "男" -> boyHeight[age]
            "girl", "female", "f", "女" -> girlHeight[age]
            else -> null
        } ?: return RecommendedSizes(null, null, null)

        val top = heightToTopPantSize(height)
        val pants = top // top & pants use same height→size rule
        val shoes = heightToShoeEU(height)

        return RecommendedSizes(top = top, pants = pants, shoes = shoes)
    }

    /**
     * Convert birth timestamp into age in years.
     * Uses 31,557,600,000 ms = average seconds per year (365.25 days).
     *
     * Output is clamped to [0, 18] because our table only covers this range.
     */
    fun ageFromBirthMillis(birth: Long, now: Long): Int {
        val years = ((now - birth) / 31557600000L).toInt()
        return years.coerceIn(0, 18)
    }
}