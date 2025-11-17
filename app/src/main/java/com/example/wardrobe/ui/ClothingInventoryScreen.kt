package com.example.wardrobe.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wardrobe.viewmodel.StatisticsViewModel
import me.bytebeats.views.charts.bar.BarChart
import me.bytebeats.views.charts.bar.BarChartData
import me.bytebeats.views.charts.bar.render.label.SimpleLabelDrawer
import me.bytebeats.views.charts.pie.PieChart
import me.bytebeats.views.charts.pie.PieChartData
import me.bytebeats.views.charts.pie.PieChartData.Slice

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClothingInventoryScreen(vm: StatisticsViewModel, navController: NavController) {
    val countByMember by vm.countByMember.collectAsState()
    val countBySeason by vm.countBySeason.collectAsState()
    val countByCategory by vm.countByCategory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clothing Inventory") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val pagerState = rememberPagerState(pageCount = { 3 })
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    when (page) {
                        0 -> StatsCard("By Member", countByMember.map { it.name to it.count })
                        1 -> StatsCard("By Season", countBySeason.map { it.season.name.replace('_', '/') to it.count })
                        2 -> StatsCard("By Category", countByCategory.map { it.category to it.count })
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(title: String, data: List<Pair<String, Int>>) {
    val chartColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.surfaceVariant
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (data.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxHeight(0.7f)
                        .fillParentMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data to display.")
                }
            }
            return@LazyColumn
        }

        item {
            // Bar Chart
            val barChartData = BarChartData(
                bars = data.mapIndexed { index, pair ->
                    BarChartData.Bar(
                        value = pair.second.toFloat(),
                        label = pair.first,
                        color = chartColors[index % chartColors.size]
                    )
                }
            )
            BarChart(
                barChartData = barChartData,
                modifier = Modifier.height(180.dp),
                labelDrawer = SimpleLabelDrawer()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }

        item {
            // Pie Chart
            val pieChartData = PieChartData(
                slices = data.mapIndexed { index, pair ->
                    Slice(
                        value = pair.second.toFloat(),
                        color = chartColors[index % chartColors.size]
                    )
                }
            )
            PieChart(
                pieChartData = pieChartData,
                modifier = Modifier.height(180.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Manual Legend for Pie Chart
        item {
            Text("Details", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }
        val total = data.sumOf { it.second }
        itemsIndexed(data) { index, pair ->
            val percentage = if (total > 0) (pair.second.toFloat() / total.toFloat()) * 100 else 0f
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(chartColors[index % chartColors.size])
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${pair.first}: ${pair.second} (${"%.1f".format(percentage)}%)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}