package com.example.wardrobe.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wardrobe.ui.components.ClothingCard
import com.example.wardrobe.ui.components.TagChips
import com.example.wardrobe.viewmodel.ViewType
import com.example.wardrobe.viewmodel.WardrobeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: WardrobeViewModel,
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    val ui by vm.uiState.collectAsState()

    val title = if (ui.memberName.isNotEmpty()) {
        "${ui.memberName}'s Wardrobe"
    } else {
        "Wardrobe"
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Text("+") } }
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = ui.query,
                onValueChange = vm::setQuery,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search description...") },
                trailingIcon = {
                    if (ui.query.isNotEmpty()) {
                        IconButton(onClick = { vm.setQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                }
            )
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

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
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

            LazyColumn {
                items(ui.items) { item ->
                    ClothingCard(item = item, onClick = { onItemClick(item.itemId) })
                    Divider()
                }
            }
        }
    }
}