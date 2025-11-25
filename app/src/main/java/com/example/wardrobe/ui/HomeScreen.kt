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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.wardrobe.data.Season
import com.example.wardrobe.data.WeatherInfo
import com.example.wardrobe.ui.components.ClothingCard
import com.example.wardrobe.ui.components.TagChips
import com.example.wardrobe.ui.components.WeatherRecommendationCard
import com.example.wardrobe.viewmodel.ViewType
import com.example.wardrobe.viewmodel.WardrobeViewModel
import com.example.wardrobe.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: WardrobeViewModel,
    weather: WeatherInfo?,          // Weather info injected from MainActivity navigation
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    val ui by vm.uiState.collectAsState()

    // Local state that mirrors the search query from the ViewModel.
    // This prevents cursor jump issues caused by direct two-way binding.
    var queryForTextField by remember { mutableStateOf(ui.query) }
    LaunchedEffect(ui.query) {
        if (queryForTextField != ui.query) {
            queryForTextField = ui.query
        }
    }

    // Toggle: show only outdated-size items?
    var showOutdatedOnly by remember { mutableStateOf(false) }
    val itemsToShow = remember(ui.items, ui.outdatedItemIds, showOutdatedOnly) {
        if (showOutdatedOnly) {
            ui.items.filter { it.itemId in ui.outdatedItemIds }
        } else ui.items
    }

    // Toggle: expand / collapse filter section
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

            // ------------------------------------------------------------
            // TOP TABS: "In Use" vs "Stored"
            // ------------------------------------------------------------
            TabRow(selectedTabIndex = ui.currentView.ordinal) {
                Tab(
                    selected = ui.currentView == ViewType.IN_USE,
                    onClick = { vm.setViewType(ViewType.IN_USE) },
                    text = { Text(stringResource(R.string.tab_in_use)) }
                )
                Tab(
                    selected = ui.currentView == ViewType.STORED,
                    onClick = { vm.setViewType(ViewType.STORED) },
                    text = { Text(stringResource(R.string.tab_stored)) }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ------------------------------------------------------------
            // WEATHER RECOMMENDATION CARD
            // Only shown in "In Use" mode
            // ------------------------------------------------------------
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

            Spacer(Modifier.height(8.dp))

            // ------------------------------------------------------------
            // FILTER / SEARCH HEADER
            // ------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.filters_search))
                TextButton(onClick = { filtersExpanded = !filtersExpanded }) {
                    Text(if (filtersExpanded) stringResource(R.string.filters_hide)
                    else stringResource(R.string.filters_show))
                    Icon(
                        imageVector = if (filtersExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            // ------------------------------------------------------------
            // FILTERS SECTION (animated expand / collapse)
            // Includes:
            //  - Search bar
            //  - Tag filter chips
            //  - Season filter chips
            // ------------------------------------------------------------
            AnimatedVisibility(visible = filtersExpanded) {
                Column {

                    // ---------------- SEARCH BAR ----------------
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
                        placeholder = { Text(stringResource(R.string.search_hint)) },
                        trailingIcon = {
                            if (queryForTextField.isNotEmpty()) {
                                IconButton(onClick = {
                                    queryForTextField = ""
                                    vm.setQuery("")
                                }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.clear_search)
                                    )
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    // ---------------- TAG FILTERS ----------------
                    Column {
                        Row(
                            modifier = Modifier.height(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.filter_by_tags), modifier = Modifier.weight(1f))
                            if (ui.selectedTagIds.isNotEmpty()) {
                                TextButton(onClick = vm::clearTagSelection) {
                                    Text(stringResource(R.string.clear))
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

                    // ---------------- SEASON FILTERS ----------------
                    Column {
                        Row(
                            modifier = Modifier.height(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.filter_by_seasons), modifier = Modifier.weight(1f))
                            if (ui.selectedSeason != null) {
                                TextButton(onClick = vm::clearSeasonFilter) {
                                    Text(stringResource(R.string.clear))
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Season.entries.forEach { season ->
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

            // ------------------------------------------------------------
            // OUTDATED SIZE NOTIFICATION CHIP
            // Appears only if the system detects size issues for the member
            // ------------------------------------------------------------
            if (ui.outdatedCount > 0) {
                AssistChip(
                    onClick = { showOutdatedOnly = !showOutdatedOnly },
                    label = {
                        val text = if (showOutdatedOnly) {
                            stringResource(R.string.outdated_items_showing, ui.outdatedCount)
                        } else {
                            stringResource(R.string.outdated_items_filter, ui.outdatedCount)
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

            // ------------------------------------------------------------
            // ITEM LIST
            // Displays clothing cards for either:
            //   ▸ all items, or
            //   ▸ only outdated items (when filtered)
            // ------------------------------------------------------------
            LazyColumn {
                items(itemsToShow) { item ->
                    ClothingCard(
                        item = item,
                        onClick = { onItemClick(item.itemId) }
                    )
                    Divider()
                }
            }
        }
    }
}