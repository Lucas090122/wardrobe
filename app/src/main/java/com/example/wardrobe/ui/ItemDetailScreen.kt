package com.example.wardrobe.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.wardrobe.viewmodel.WardrobeViewModel
import com.example.wardrobe.ui.components.TagChips
import kotlin.math.max
import androidx.compose.ui.platform.LocalConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    vm: WardrobeViewModel,
    itemId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val data by vm.itemFlow(itemId).collectAsState(initial = null)
    var showConfirm by remember { mutableStateOf(false) } // Hoisted to screen scope

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
                actions = {
                    TextButton(onClick = onEdit) { Text("Edit") }
                    TextButton(onClick = { showConfirm = true }) { Text("Delete") } // The button is here
                }
            )
        }
    ) { padding ->
        if (data == null) {
            Box(Modifier.padding(padding).fillMaxSize()) { Text("Loading...") }
        } else {
            val item = data!!.item
            val tags = data!!.tags

            // Screen height, used to limit the maximum height of the image (e.g., not exceeding 60%)
            val screenH = LocalConfiguration.current.screenHeightDp.dp
            val maxImageH = screenH * 0.6f

            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    val uri = item.imageUri?.let { android.net.Uri.parse(it) }
                    if (uri != null) {
                        var aspect by remember { mutableFloatStateOf(1.6f) }

                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                // Adapt to the aspect ratio, but do not exceed 60% of the screen height
                                .heightIn(max = maxImageH),
                            contentScale = ContentScale.Fit,
                            onSuccess = { success ->
                                val d = success.result.drawable
                                val w = max(1, d.intrinsicWidth)
                                val h = max(1, d.intrinsicHeight)
                                aspect = w.toFloat() / h
                            }
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
            }
        }
    }

    // Place the dialog outside the Scaffold (within the same Composable)
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