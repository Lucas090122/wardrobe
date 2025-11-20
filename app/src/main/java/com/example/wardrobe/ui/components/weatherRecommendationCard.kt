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
                text = "Today's Recommendation",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            // ---------- 已确认：固定为“今日穿搭” ----------
            if (fixedOutfit != null) {
                val outfit = fixedOutfit!!
                Text(
                    text = "Today's Outfit (Confirmed)",
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
                Text("Weather information is unavailable. Recommendations cannot be provided.")
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
                    text = "Could not generate an outfit. Please check your clothing items.",
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
                        Text("Another one")
                    }
                }

                Button(
                    onClick = {
                        fixedOutfit = outfit      // 之后变成“今日穿搭”固定页
                        onConfirmOutfit(outfitItems)
                    }
                ) {
                    Text("Confirm for Today")
                }
            }
        }
    }
}
