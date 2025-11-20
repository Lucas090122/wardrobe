package com.example.wardrobe.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wardrobe.data.Season
import com.example.wardrobe.data.WeatherInfo
import com.example.wardrobe.ui.components.ClothingCard
import com.example.wardrobe.ui.components.TagChips
import com.example.wardrobe.ui.components.WeatherRecommendationCard
import com.example.wardrobe.viewmodel.ViewType
import com.example.wardrobe.viewmodel.WardrobeViewModel
import com.example.wardrobe.ui.util.WeatherRecommender


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: WardrobeViewModel,
    weather: WeatherInfo?,          // ← 新增参数
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    val ui by vm.uiState.collectAsState()

    var queryForTextField by remember { mutableStateOf(ui.query) }
    LaunchedEffect(ui.query) {
        if (queryForTextField != ui.query) {
            queryForTextField = ui.query
        }
    }

    var showOutdatedOnly by remember { mutableStateOf(false) }
    val itemsToShow = remember(ui.items, ui.outdatedItemIds, showOutdatedOnly) {
        if (showOutdatedOnly) {
            ui.items.filter { it.itemId in ui.outdatedItemIds }
        } else ui.items
    }

    var filtersExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) { Text("+") }
        }
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(8.dp))

            TabRow(selectedTabIndex = ui.currentView.ordinal) {
                Tab(
                    selected = ui.currentView == ViewType.IN_USE,
                    onClick = { vm.setViewType(ViewType.IN_USE) },
                    text = { Text("In Use") }
                )
                Tab(
                    selected = ui.currentView == ViewType.STORED,
                    onClick = { vm.setViewType(ViewType.STORED) },
                    text = { Text("Stored") }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ----- 推荐卡片 -----
//            WeatherRecommendationCard(
//                weather = weather,
//                items = ui.items,
//                onItemClick = onItemClick,
//                onConfirmOutfit = { outfitItems ->
//                    vm.markOutfitAsWorn(outfitItems)
//                }
//            )
            if (ui.currentView == ViewType.IN_USE) {
                WeatherRecommendationCard(
                    weather = weather,
                    items = ui.items,
                    onItemClick = onItemClick,
                    onConfirmOutfit = { outfitItems ->
                        vm.markOutfitAsWorn(outfitItems)
                    }
                )

                Spacer(Modifier.height(8.dp))
            }

            // ----------------------

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Filters & search")
                TextButton(onClick = { filtersExpanded = !filtersExpanded }) {
                    Text(if (filtersExpanded) "Hide" else "Show")
                    Icon(
                        imageVector = if (filtersExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(visible = filtersExpanded) {
                Column {
                    OutlinedTextField(
                        value = queryForTextField,
                        onValueChange = {
                            queryForTextField = it
                            vm.setQuery(it)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        placeholder = { Text("Search description...") },
                        trailingIcon = {
                            if (queryForTextField.isNotEmpty()) {
                                IconButton(onClick = {
                                    queryForTextField = ""
                                    vm.setQuery("")
                                }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    // tags
                    Column {
                        Row(
                            modifier = Modifier.height(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Filter by tags", modifier = Modifier.weight(1f))
                            if (ui.selectedTagIds.isNotEmpty()) {
                                TextButton(onClick = vm::clearTagSelection) {
                                    Text("Clear")
                                }
                            }
                        }
                        TagChips(
                            tags = ui.tags,
                            selectedIds = ui.selectedTagIds,
                            onToggle = vm::toggleTag,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // seasons
                    Column {
                        Row(
                            modifier = Modifier.height(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Filter by seasons", modifier = Modifier.weight(1f))
                            if (ui.selectedSeason != null) {
                                TextButton(onClick = vm::clearSeasonFilter) {
                                    Text("Clear")
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Season.values().forEach { season ->
                                FilterChip(
                                    selected = ui.selectedSeason == season,
                                    onClick = { vm.setSeasonFilter(season) },
                                    label = { Text(season.name.replace('_', '/')) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }

            if (ui.outdatedCount > 0) {
                AssistChip(
                    onClick = { showOutdatedOnly = !showOutdatedOnly },
                    label = {
                        val text = if (showOutdatedOnly) {
                            "${ui.outdatedCount} items may be too small (showing)"
                        } else {
                            "${ui.outdatedCount} items may be too small (tap to filter)"
                        }
                        Text(text)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(4.dp))

            LazyColumn {
                items(itemsToShow) { item ->
                    ClothingCard(item = item, onClick = {
                        onItemClick(item.itemId)
                    })
                    Divider()
                }
            }
        }
    }
}
