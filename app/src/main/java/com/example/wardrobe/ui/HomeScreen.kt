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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wardrobe.data.ClothingItem
import com.example.wardrobe.data.Tag
import com.example.wardrobe.ui.components.ClothingCard
import com.example.wardrobe.ui.components.TagChips

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    tags: List<Tag>,
    selectedTagIds: Set<Long>,
    query: String,
    items: List<ClothingItem>,
    onToggleTag: (Long) -> Unit,
    onQueryChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Wardrobe") }) },
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Text("+") } }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search description...") }
            )
            Spacer(Modifier.height(8.dp))

            TagChips(
                tags = tags,
                selectedIds = selectedTagIds,
                onToggle = onToggleTag,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(items) { item ->
                    ClothingCard(item = item, onClick = { onItemClick(item.itemId) })
                    Divider()
                }
            }
        }
    }
}