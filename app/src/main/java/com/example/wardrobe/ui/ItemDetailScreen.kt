package com.example.wardrobe.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.wardrobe.ui.components.TagChips
import com.example.wardrobe.viewmodel.WardrobeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    vm: WardrobeViewModel,
    itemId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val itemData by vm.itemFlow(itemId).collectAsState(initial = null)
    val uiState by vm.uiState.collectAsState()
    var showConfirm by remember { mutableStateOf(false) } // Hoisted to screen scope

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
                actions = {
                    TextButton(onClick = onEdit) { Text("Edit") }
                    TextButton(onClick = { showConfirm = true }) { Text("Delete") }
                }
            )
        }
    ) { padding ->
        if (itemData == null) {
            Box(Modifier.padding(padding).fillMaxSize()) { Text("Loading...") }
        } else {
            val item = itemData!!.item
            val tags = itemData!!.tags

            val screenH = LocalConfiguration.current.screenHeightDp.dp
            val maxImageH = screenH * 0.6f

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    val uri = item.imageUri?.let { android.net.Uri.parse(it) }
                    if (uri != null) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .heightIn(max = maxImageH),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                item {
                    Text(text = item.description, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = android.text.format.DateFormat
                            .format("yyyy-MM-dd", item.createdAt).toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (tags.isNotEmpty()) {
                    item {
                        Text("Tags", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        TagChips(
                            tags = tags,
                            selectedIds = tags.map { it.tagId }.toSet(),
                            onToggle = {},
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Show storage location if applicable
                if (item.stored) {
                    item {
                        val locationName = item.locationId?.let { locId ->
                            uiState.locations.find { it.locationId == locId }?.name
                        }
                        val text = if (locationName != null) {
                            "Stored in: $locationName"
                        } else {
                            "Stored (No location assigned)"
                        }
                        Text(text, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this item? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteItem(itemId)
                    showConfirm = false
                    onBack() // Go back to home
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
            }
        )
    }
}