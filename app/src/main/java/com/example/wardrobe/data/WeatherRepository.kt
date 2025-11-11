package com.example.wardrobe.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class WeatherInfo(
    val temperature: Double,
    val icon: String
)

class WeatherRepository(private val context: Context) {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentWeather(): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc: Location? = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val lat = loc?.latitude ?: 60.17  // Helsinki fallback
            val lon = loc?.longitude ?: 24.94

            // Open-Meteo：免费、无需 key
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true"
            val resp = URL(url).readText()
            val cur = JSONObject(resp).getJSONObject("current_weather")

            val temp = cur.getDouble("temperature")
            val code = cur.getInt("weathercode")

            WeatherInfo(temperature = temp, icon = mapWeatherCodeToIcon(code))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun mapWeatherCodeToIcon(code: Int): String = when (code) {
        in 0..2 -> "☀️"
        in 3..48 -> "⛅"
        in 49..67 -> "🌧"
        in 68..77 -> "❄️"
        in 78..99 -> "🌩"
        else -> "☁️"
    }
}


