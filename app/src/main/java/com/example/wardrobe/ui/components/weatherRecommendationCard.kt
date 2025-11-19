//package com.example.wardrobe.ui.components
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.example.wardrobe.data.ClothingItem
//import com.example.wardrobe.data.WeatherInfo
//import com.example.wardrobe.ui.util.WeatherRecommender
//
//@Composable
//fun WeatherRecommendationCard(
//    weather: WeatherInfo?,
//    items: List<ClothingItem>,
//    onItemClick: (Long) -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        elevation = CardDefaults.cardElevation(3.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//
//            Text(
//                text = "Today's Outfit Recommendation",
//                style = MaterialTheme.typography.titleMedium
//            )
//
//            Spacer(Modifier.height(8.dp))
//
//            if (weather == null) {
//                Text("Weather unavailable")
//            }else{
//                // 天气显示
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(
//                        text = weather.icon,
//                        style = MaterialTheme.typography.headlineMedium
//                    )
//                    Spacer(Modifier.width(12.dp))
//                    Column {
//                        Text("Temp: ${weather.temperature.toInt()}°C")
//                        Text("Feels like: ${weather.apparentTemperature.toInt()}°C")
//                        Text("Wind: ${weather.windSpeed} m/s")
//                        Text("UV Index: ${weather.uvIndex}")
//                    }
//                }
//
//                Spacer(Modifier.height(12.dp))
//
//                val result = WeatherRecommender.recommend(weather, items)
//
//                if (result.recommended.isEmpty()) {
//                    Text("No suitable items found for today.")
//                    Text(
//                        result.reason,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                } else {
//                    Text(
//                        result.reason,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Spacer(Modifier.height(8.dp))
//
//                    result.recommended.forEach { item ->
//                        TextButton(onClick = { onItemClick(item.itemId) }) {
//                            Text("• ${item.description}")
//                        }
//                    }
//                }
//            }
//
//
//        }
//    }
//}
//1-----------------
//package com.example.wardrobe.ui.components
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.example.wardrobe.data.ClothingItem
//import com.example.wardrobe.data.WeatherInfo
//import com.example.wardrobe.ui.util.WeatherRecommender
//
//@Composable
//fun WeatherRecommendationCard(
//    weather: WeatherInfo?,
//    items: List<ClothingItem>,
//    onItemClick: (Long) -> Unit,
//    onConfirmOutfit: (List<ClothingItem>) -> Unit
//) {
//    var lastOutfit by remember { mutableStateOf<WeatherRecommender.Outfit?>(null) }
//    var result by remember { mutableStateOf<WeatherRecommender.Result?>(null) }
//
//    // 天气或衣物列表变化时，重新计算一次推荐
//    LaunchedEffect(weather, items) {
//        result = weather?.let { WeatherRecommender.recommend(it, items, lastOutfit) }
//    }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        elevation = CardDefaults.cardElevation(3.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//
//            Text(
//                text = "Today's Outfit Recommendation",
//                style = MaterialTheme.typography.titleMedium
//            )
//
//            Spacer(Modifier.height(8.dp))
//
//            if (weather == null) {
//                Text("Weather unavailable")
//                return@Column
//            }
//
//            val current = result
//            if (current == null) {
//                Text("No recommendation yet.")
//                return@Column
//            }
//
//            if (current.outfit == null) {
//                Text(current.reason)
//                return@Column
//            }
//
//            val outfit = current.outfit
//
//            // 简单的天气说明
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Text(
//                    text = weather.icon,
//                    style = MaterialTheme.typography.headlineMedium
//                )
//                Column {
//                    Text("Temp: ${weather.temperature.toInt()}°C")
//                    Text("Feels like: ${weather.apparentTemperature.toInt()}°C")
//                    Text("Wind: ${weather.windSpeed} m/s, UV: ${weather.uvIndex}")
//                }
//            }
//
//            Spacer(Modifier.height(8.dp))
//
//            Text(
//                text = current.reason,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            Spacer(Modifier.height(8.dp))
//
//            // 展示三件衣服
//            OutfitItem(label = "Top", item = outfit.top, onClick = onItemClick)
//            OutfitItem(label = "Pants", item = outfit.pants, onClick = onItemClick)
//            OutfitItem(label = "Shoes", item = outfit.shoes, onClick = onItemClick)
//
//            Spacer(Modifier.height(12.dp))
//
//            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                if (current.canRefresh) {
//                    Button(onClick = {
//                        lastOutfit = outfit
//                        result = WeatherRecommender.recommend(
//                            weather = weather,
//                            items = items,
//                            lastOutfit = lastOutfit
//                        )
//                    }) {
//                        Text("换一套")
//                    }
//                }
//
//                Button(onClick = {
//                    val list = listOfNotNull(outfit.top, outfit.pants, outfit.shoes)
//                    onConfirmOutfit(list)
//                    lastOutfit = outfit
//                }) {
//                    Text("就这套")
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun OutfitItem(
//    label: String,
//    item: ClothingItem?,
//    onClick: (Long) -> Unit
//) {
//    if (item == null) return
//
//    TextButton(onClick = { onClick(item.itemId) }) {
//        Text("$label: ${item.description}")
//    }
//}
//2----------
package com.example.wardrobe.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.WeatherInfo
import com.example.wardrobe.ui.util.WeatherRecommender

@Composable
fun WeatherRecommendationCard(
    weather: WeatherInfo?,
    items: List<ClothingItem>,
    onItemClick: (Long) -> Unit,
    onConfirmOutfit: (List<ClothingItem>) -> Unit
) {
    var lastOutfit by remember { mutableStateOf<WeatherRecommender.Outfit?>(null) }
    var result by remember { mutableStateOf<WeatherRecommender.Result?>(null) }

    // 当天气或衣物发生变化时重算推荐
    LaunchedEffect(weather, items) {
        result = weather?.let {
            WeatherRecommender.recommend(
                weather = it,
                items = items,
                lastOutfit = lastOutfit
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                "Today's Outfit Recommendation",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            if (weather == null) {
                Text("Weather unavailable.")
                return@Column
            }

            val res = result
            if (res == null || res.outfit == null) {
                Text(res?.reason ?: "No outfit available.")
                return@Column
            }

            val outfit = res.outfit

            // ======== 图片展示三件衣服 ========
            OutfitPreviewRow(
                top = outfit.top,
                pants = outfit.pants,
                shoes = outfit.shoes,
                onItemClick = onItemClick
            )
            // =================================

            Spacer(Modifier.height(12.dp))

            Text(
                res.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            // ===== 按钮区域 =====
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                if (res.canRefresh) {
                    Button(onClick = {
                        lastOutfit = outfit
                        result = WeatherRecommender.recommend(
                            weather = weather,
                            items = items,
                            lastOutfit = lastOutfit
                        )
                    }) { Text("换一套") }
                }

                Button(onClick = {
                    val list = listOfNotNull(outfit.top, outfit.pants, outfit.shoes)
                    onConfirmOutfit(list)
                    lastOutfit = outfit
                }) {
                    Text("就这套")
                }
            }
        }
    }
}

@Composable
private fun OutfitPreviewRow(
    top: ClothingItem?,
    pants: ClothingItem?,
    shoes: ClothingItem?,
    onItemClick: (Long) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ItemImageCard(item = top, label = "Top", onItemClick)
        ItemImageCard(item = pants, label = "Pants", onItemClick)
        ItemImageCard(item = shoes, label = "Shoes", onItemClick)
    }
}

@Composable
private fun ItemImageCard(
    item: ClothingItem?,
    label: String,
    onItemClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { if (item != null) onItemClick(item.itemId) }
    ) {
        if (item == null || item.imageUri.isNullOrBlank()) {
            // 无图片 → 显示文字占位
            Box(contentAlignment = Alignment.Center) {
                Text("$label\n(No Image)")
            }
        } else {
            // 有图片 → 显示衣服照片
            AsyncImage(
                model = item.imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
