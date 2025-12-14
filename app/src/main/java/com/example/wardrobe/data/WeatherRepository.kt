package com.example.wardrobe.data

import android.content.Context
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class WeatherRepository(private val context: Context) {
    // Fetch current weather using device location
    @androidx.annotation.RequiresPermission(
        allOf = [
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ]
    )
    suspend fun getCurrentWeather(): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc: Location? = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            // Fallback to Helsinki coordinates if location is unavailable
            val lat = loc?.latitude ?: 60.17
            val lon = loc?.longitude ?: 24.94

            val url =
                "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,apparent_temperature,weather_code,wind_speed_10m,uv_index&timezone=auto"

            val response = URL(url).readText()
            val json = JSONObject(response)
            val current = json.getJSONObject("current")

            val temp = current.getDouble("temperature_2m")
            val apparent = current.getDouble("apparent_temperature")
            val wind = current.getDouble("wind_speed_10m")
            val uv = current.optDouble("uv_index", 0.0)
            val code = current.getInt("weather_code")

            WeatherInfo(
                temperature = temp,
                apparentTemperature = apparent,
                windSpeed = wind,
                uvIndex = uv,
                weatherCode = code,
                icon = mapWeatherCodeToIcon(code)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Weather code → icon mapping
    private fun mapWeatherCodeToIcon(code: Int): String {
        return when (code) {
            0 -> "☀️" // Clear sky
            1, 2 -> "🌤"
            3 -> "⛅"
            45, 48 -> "🌫"
            51, 53, 55 -> "🌦"
            61, 63, 65 -> "🌧"
            66, 67 -> "🌧❄️"
            71, 73, 75, 77 -> "❄️"
            80, 81, 82 -> "🌧"
            95, 96, 99 -> "⛈"
            else -> "☁️"
        }
    }
}
