package com.example.wardrobe.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.wardrobe.R
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
    modifier: Modifier = Modifier,
    confirmedOutfit: List<ClothingItem>?
) {
    var isExpanded by remember { mutableStateOf(true) }

    // Used for “Another one”:
    // We remember the last outfit to avoid picking it again.
    var lastOutfit by remember { mutableStateOf<WeatherRecommender.Outfit?>(null) }

    // Changing this value re-triggers the recommendation calculation.
    var refreshSeed by remember { mutableStateOf(0) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        val padding = if (isExpanded) PaddingValues(16.dp) else PaddingValues(horizontal = 16.dp, vertical = 4.dp)

        Column(Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weather_title_recommendation),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(Modifier.height(8.dp))

                    // ----------------------------------------------------------
                    // 1. FIXED MODE — after the user confirms today’s outfit
                    // ----------------------------------------------------------
                    if (confirmedOutfit != null) {
                        Text(
                            text = stringResource(R.string.weather_title_confirmed),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        confirmedOutfit.forEachIndexed { index, item ->
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
                        Text(stringResource(R.string.weather_unavailable))
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
                            text = stringResource(R.string.weather_cannot_generate),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = result.localizedMessage(),
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
                        text = result.localizedMessage(),
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
                                Text(stringResource(R.string.weather_button_another_one))
                            }
                        }

                        Button(
                            onClick = {
                                onConfirmOutfit(outfitItems)  // Notify VM
                            }
                        ) {
                            Text(stringResource(R.string.weather_button_confirm_today))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherRecommender.Result.localizedMessage(): String =
    when (reasonCode) {
        WeatherRecommender.ReasonCode.BASIC ->
            stringResource(R.string.weather_reason_basic, temperatureRounded)

        WeatherRecommender.ReasonCode.AVOIDING_RECENT ->
            stringResource(R.string.weather_reason_avoiding_recent, temperatureRounded)

        WeatherRecommender.ReasonCode.NO_MATCH ->
            stringResource(R.string.weather_reason_no_match)

        WeatherRecommender.ReasonCode.MISSING_CATEGORY ->
            stringResource(R.string.weather_reason_missing_category)

        WeatherRecommender.ReasonCode.NO_COMBINATIONS ->
            stringResource(R.string.weather_reason_no_combinations)
    }
