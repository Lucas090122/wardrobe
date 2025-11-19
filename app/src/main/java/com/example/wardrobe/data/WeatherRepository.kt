//package com.example.wardrobe.data
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.location.Location
//import android.location.LocationManager
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import org.json.JSONObject
//import java.net.URL
//
//data class WeatherInfo(
//    val temperature: Double,
//    val icon: String
//)
//
//class WeatherRepository(private val context: Context) {
//
//    @SuppressLint("MissingPermission")
//    suspend fun getCurrentWeather(): WeatherInfo? = withContext(Dispatchers.IO) {
//        try {
//            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            val loc: Location? = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
//            val lat = loc?.latitude ?: 60.17  // Helsinki fallback
//            val lon = loc?.longitude ?: 24.94
//
//            // Open-Meteo：免费、无需 key
//            val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true"
//            val resp = URL(url).readText()
//            val cur = JSONObject(resp).getJSONObject("current_weather")
//
//            val temp = cur.getDouble("temperature")
//            val code = cur.getInt("weathercode")
//
//            WeatherInfo(temperature = temp, icon = mapWeatherCodeToIcon(code))
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    private fun mapWeatherCodeToIcon(code: Int): String = when (code) {
//        in 0..2 -> "☀️"
//        in 3..48 -> "⛅"
//        in 49..67 -> "🌧"
//        in 68..77 -> "❄️"
//        in 78..99 -> "🌩"
//        else -> "☁️"
//    }
//}
//
//
//1--------------
package com.example.wardrobe.data

import android.content.Context
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class WeatherRepository(private val context: Context) {
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
//2---------------
//package com.example.wardrobe.data
//
//import android.content.Context
//import android.util.Log
//import org.json.JSONObject
//import java.net.URL
//import javax.net.ssl.HttpsURLConnection
//
//class WeatherRepository(private val context: Context) {
//
//    private val baseUrl = "https://api.open-meteo.com/v1/"
//
//    fun getCurrentWeather(
//        lat: Double,
//        lon: Double,
//        onSuccess: (WeatherInfo) -> Unit,
//        onError: (Exception) -> Unit
//    ) {
//        Thread {
//            try {
//                val urlString =
//                    "${baseUrl}forecast?latitude=$lat&longitude=$lon&current=temperature_2m,apparent_temperature,weather_code,wind_speed_10m,uv_index&wind_speed_unit=ms"
//
//                val url = URL(urlString)
//                val conn = url.openConnection() as HttpsURLConnection
//                conn.requestMethod = "GET"
//
//                val stream = conn.inputStream
//                val response = stream.bufferedReader().readText()
//
//                val json = JSONObject(response)
//
//                val current = json.getJSONObject("current")
//                val temperature = current.optDouble("temperature_2m", 0.0)
//
//                // ⭐ Fallback 开始 —— 任何字段不存在都不会出错
//                val apparent = current.optDouble("apparent_temperature", temperature)
//                val wind = current.optDouble("wind_speed_10m", 0.0)
//                val uv = current.optDouble("uv_index", 0.0)
//                val code = current.optInt("weather_code", 0)
//                // ⭐ Fallback 结束
//
//                val icon = WeatherCodeMapper.toIcon(code)
//
//                val info = WeatherInfo(
//                    temperature = temperature,
//                    apparentTemperature = apparent,
//                    windSpeed = wind,
//                    uvIndex = uv,
//                    weatherCode = code,
//                    icon = icon
//                )
//
//                onSuccess(info)
//
//            } catch (e: Exception) {
//                onError(e)
//            }
//        }.start()
//    }
//}
//
//
//
//
//
//// ⭐ 我给你补了 WeatherCodeMapper（若你已有可跳过）
//object WeatherCodeMapper {
//    fun toIcon(code: Int): String {
//        return when (code) {
//            in 0..1 -> "☀️"
//            in 2..3 -> "⛅"
//            in 45..48 -> "🌫"
//            in 51..67 -> "🌧"
//            in 71..77 -> "❄️"
//            in 80..82 -> "🌧"
//            else -> "☁️"
//        }
//    }
//}
//
