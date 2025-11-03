package com.example.wardrobe.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wardrobe.ui.components.ClothingCard
import com.example.wardrobe.ui.components.TagChips
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
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = ui.query,
                onValueChange = vm::setQuery,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search description...") }
            )
            Spacer(Modifier.height(8.dp))

            TagChips(
                tags = ui.tags,
                selectedIds = ui.selectedTagIds,
                onToggle = vm::toggleTag,
                modifier = Modifier.fillMaxWidth()
            )
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