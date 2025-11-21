package com.example.wardrobe.data

data class WeatherInfo(
    val temperature: Double,
    val apparentTemperature: Double,
    val windSpeed: Double,
    val uvIndex: Double,
    val weatherCode: Int,
    val icon: String
) {
    // UV level
    val uvLevel: String
        get() = when {
            uvIndex < 3 -> "Low"
            uvIndex < 6 -> "Moderate"
            uvIndex < 8 -> "High"
            uvIndex < 11 -> "Very High"
            else -> "Extreme"
        }
}
