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
import com.example.wardrobe.util.WeatherRecommender

/**
 * UI component that displays:
 * - The daily weather-based clothing recommendation
 * - A preview of the suggested outfit (top + pants + shoes)
 * - Buttons to refresh the outfit or confirm it as today's choice
 *
 * The card behaves like a "widget":
 * ▸ If the user has confirmed today's outfit → the card becomes fixed
 * ▸ If not confirmed → user can refresh or confirm the suggested outfit
 */
@Composable
fun WeatherRecommendationCard(
    weather: WeatherInfo?,
    items: List<ClothingItem>,
    onItemClick: (Long) -> Unit,
    onConfirmOutfit: (List<ClothingItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Whether the user has confirmed today's outfit.
    // Once confirmed, the UI switches to a "locked" mode.
    var fixedOutfit by remember { mutableStateOf<WeatherRecommender.Outfit?>(null) }

    // Used for “Another one”:
    // We remember the last outfit to avoid picking it again.
    var lastOutfit by remember { mutableStateOf<WeatherRecommender.Outfit?>(null) }

    // Changing this value re-triggers the recommendation calculation.
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

            // ----------------------------------------------------------
            // 1. FIXED MODE — after the user confirms today’s outfit
            // ----------------------------------------------------------
            if (fixedOutfit != null) {
                val outfit = fixedOutfit!!

                Text(
                    text = "Today's Outfit (Confirmed)",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))

                // Ensure we display existing valid items only
                val outfitItems = listOfNotNull(outfit.top, outfit.pants, outfit.shoes)

                outfitItems.forEachIndexed { index, item ->
                    if (index > 0) {
                        Divider(Modifier.padding(vertical = 4.dp))
                    }
                    // Using ClothingCard to display with image preview
                    ClothingCard(
                        item = item,
                        onClick = { onItemClick(item.itemId) }
                    )
                }

                // Fixed mode ends here — no refresh or confirm buttons
                return@Column
            }

            // ----------------------------------------------------------
            // 2. NORMAL MODE — weather-based recommendation
            // ----------------------------------------------------------
            if (weather == null) {
                Text("Weather information is unavailable. Recommendations cannot be provided.")
                return@Column
            }

            // Recalculate recommendation when weather/items/refreshSeed/lastOutfit changes
            val result = remember(weather, items, refreshSeed, lastOutfit) {
                WeatherRecommender.recommend(
                    weather = weather,
                    items = items,
                    lastOutfit = lastOutfit
                )
            }

            // When no outfit can be generated (missing categories etc.)
            if (result.outfit == null) {
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

            // Display explanation ("Feels like X°C. Avoiding recently worn clothes.")
            Text(
                text = result.reason,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(8.dp))

            // Display the recommended items with their thumbnails
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

            // ----------------------------------------------------------
            // 3. Buttons row: "Another one" & "Confirm"
            // ----------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Show "Another one" only if there is more than 1 candidate outfit
                if (result.canRefresh) {
                    TextButton(
                        onClick = {
                            lastOutfit = outfit     // Remember the previous one
                            refreshSeed++           // Trigger a new recommendation
                        }
                    ) {
                        Text("Another one")
                    }
                }

                Button(
                    onClick = {
                        fixedOutfit = outfit          // Lock the outfit for today
                        onConfirmOutfit(outfitItems)  // Notify VM
                    }
                ) {
                    Text("Confirm for Today")
                }
            }
        }
    }
}