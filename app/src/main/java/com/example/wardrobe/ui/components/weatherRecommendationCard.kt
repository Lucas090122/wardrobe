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
//package com.example.wardrobe.ui.components
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import coil.compose.AsyncImage
//import com.example.wardrobe.data.ClothingItem
//import com.example.wardrobe.data.WeatherInfo
//import com.example.wardrobe.ui.util.WeatherRecommender

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
//    // 当天气或衣物发生变化时重算推荐
//    LaunchedEffect(weather, items) {
//        result = weather?.let {
//            WeatherRecommender.recommend(
//                weather = it,
//                items = items,
//                lastOutfit = lastOutfit
//            )
//        }
//    }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        shape = RoundedCornerShape(16.dp),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//
//            Text(
//                "Today's Outfit Recommendation",
//                style = MaterialTheme.typography.titleMedium
//            )
//
//            Spacer(Modifier.height(12.dp))
//
//            if (weather == null) {
//                Text("Weather unavailable.")
//                return@Column
//            }
//
//            val res = result
//            if (res == null || res.outfit == null) {
//                Text(res?.reason ?: "No outfit available.")
//                return@Column
//            }
//
//            val outfit = res.outfit
//
//            // ======== 图片展示三件衣服 ========
//            OutfitPreviewRow(
//                top = outfit.top,
//                pants = outfit.pants,
//                shoes = outfit.shoes,
//                onItemClick = onItemClick
//            )
//            // =================================
//
//            Spacer(Modifier.height(12.dp))
//
//            Text(
//                res.reason,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            Spacer(Modifier.height(12.dp))
//
//            // ===== 按钮区域 =====
//            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//
//                if (res.canRefresh) {
//                    Button(onClick = {
//                        lastOutfit = outfit
//                        result = WeatherRecommender.recommend(
//                            weather = weather,
//                            items = items,
//                            lastOutfit = lastOutfit
//                        )
//                    }) { Text("换一套") }
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
//fun WeatherRecommendationCard(
//    weather: WeatherInfo?,
//    items: List<ClothingItem>,
//    onItemClick: (Long) -> Unit,
//    onConfirmOutfit: (List<ClothingItem>) -> Unit
//) {
//    if (weather == null) {
//        Text("天气不可用")
//        return
//    }
//
//    val result = remember(weather, items) {
//        WeatherRecommender.recommend(weather, items)
//    }
//
//    var showDebug by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(Modifier.padding(16.dp)) {
//            Text("今日推荐", style = MaterialTheme.typography.titleMedium)
//            Spacer(Modifier.height(8.dp))
//
//            Text(result.reason)
//
//            if (result.outfit == null) {
//                Spacer(Modifier.height(8.dp))
//                Text("无法生成穿搭，请查看调试信息。")
//            } else {
//                Spacer(Modifier.height(8.dp))
//
//                result.outfit.top?.let {
//                    Text("上衣：${it.description}")
//                }
//                result.outfit.pants?.let {
//                    Text("裤子：${it.description}")
//                }
//                result.outfit.shoes?.let {
//                    Text("鞋子：${it.description}")
//                }
//
//                Button(
//                    onClick = {
//                        onConfirmOutfit(
//                            listOfNotNull(
//                                result.outfit.top,
//                                result.outfit.pants,
//                                result.outfit.shoes
//                            )
//                        )
//                    },
//                    modifier = Modifier.padding(top = 8.dp)
//                ) {
//                    Text("确认穿搭")
//                }
//            }
//
//            Spacer(Modifier.height(12.dp))
//
//            // ---- Debug Section ----
//            TextButton(onClick = { showDebug = !showDebug }) {
//                Text(if (showDebug) "隐藏调试信息" else "显示调试信息")
//            }
//
//            AnimatedVisibility(showDebug) {
//                Text(
//                    result.debugLog,
//                    modifier = Modifier.padding(top = 8.dp)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun OutfitPreviewRow(
//    top: ClothingItem?,
//    pants: ClothingItem?,
//    shoes: ClothingItem?,
//    onItemClick: (Long) -> Unit
//) {
//    Row(
//        horizontalArrangement = Arrangement.spacedBy(12.dp),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        ItemImageCard(item = top, label = "Top", onItemClick)
//        ItemImageCard(item = pants, label = "Pants", onItemClick)
//        ItemImageCard(item = shoes, label = "Shoes", onItemClick)
//    }
//}
//
//@Composable
//private fun ItemImageCard(
//    item: ClothingItem?,
//    label: String,
//    onItemClick: (Long) -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .size(100.dp)
//            .padding(4.dp),
//        shape = RoundedCornerShape(12.dp),
//        onClick = { if (item != null) onItemClick(item.itemId) }
//    ) {
//        if (item == null || item.imageUri.isNullOrBlank()) {
//            // 无图片 → 显示文字占位
//            Box(contentAlignment = Alignment.Center) {
//                Text("$label\n(No Image)")
//            }
//        } else {
//            // 有图片 → 显示衣服照片
//            AsyncImage(
//                model = item.imageUri,
//                contentDescription = null,
//                modifier = Modifier.fillMaxSize()
//            )
//        }
//    }
//}

//3-----
package com.example.wardrobe.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.WeatherInfo
import com.example.wardrobe.ui.util.WeatherRecommender

@Composable
fun WeatherRecommendationCard(
    weather: WeatherInfo?,
    items: List<ClothingItem>,
    onItemClick: (Long) -> Unit,
    onConfirmOutfit: (List<ClothingItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    // 今天是否已经确认了一套穿搭
    var fixedOutfit by remember { mutableStateOf<WeatherRecommender.Outfit?>(null) }

    // “换一套”使用：记住上一套，以便避免重复
    var lastOutfit by remember { mutableStateOf<WeatherRecommender.Outfit?>(null) }
    var refreshSeed by remember { mutableStateOf(0) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "今日推荐",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            // ---------- 已确认：固定为“今日穿搭” ----------
            if (fixedOutfit != null) {
                val outfit = fixedOutfit!!
                Text(
                    text = "今日穿搭（已确认）",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))

                val outfitItems = listOfNotNull(outfit.top, outfit.pants, outfit.shoes)
                outfitItems.forEachIndexed { index, item ->
                    if (index > 0) {
                        Divider(Modifier.padding(vertical = 4.dp))
                    }
                    ClothingCard(
                        item = item,
                        onClick = { onItemClick(item.itemId) }
                    )
                }

                // 此状态下不再出现“换一套 / 确认”按钮，卡片变成当天固定页
                return@Column
            }

            // ---------- 还未确认：正常推荐逻辑 ----------
            if (weather == null) {
                Text("天气信息不可用，暂时无法推荐穿搭。")
                return@Column
            }

            val result = remember(weather, items, refreshSeed, lastOutfit) {
                WeatherRecommender.recommend(
                    weather = weather,
                    items = items,
                    lastOutfit = lastOutfit
                )
            }

            if (result.outfit == null) {
                // 没有搭配成功的情况：依然展示原因
                Text(
                    text = "无法生成穿搭，请检查衣物信息。",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = result.reason,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                return@Column
            }

            val outfit = result.outfit
            val outfitItems = listOfNotNull(outfit.top, outfit.pants, outfit.shoes)

            // 推荐理由
            Text(
                text = result.reason,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(8.dp))

            // ---------- 用 ClothingCard 展示图片（解决“推荐不显示图片”） ----------
            outfitItems.forEachIndexed { index, item ->
                if (index > 0) {
                    Divider(Modifier.padding(vertical = 4.dp))
                }
                ClothingCard(
                    item = item,
                    onClick = { onItemClick(item.itemId) }
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // “换一套”按钮（只有在可刷新时出现）
                if (result.canRefresh) {
                    TextButton(
                        onClick = {
                            lastOutfit = outfit
                            refreshSeed++      // 触发重新计算推荐
                        }
                    ) {
                        Text("换一套")
                    }
                }

                Button(
                    onClick = {
                        fixedOutfit = outfit      // 之后变成“今日穿搭”固定页
                        onConfirmOutfit(outfitItems)
                    }
                ) {
                    Text("确认今天就穿这套")
                }
            }
        }
    }
}
