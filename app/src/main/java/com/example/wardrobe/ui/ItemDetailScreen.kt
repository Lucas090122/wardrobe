package com.example.wardrobe.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.wardrobe.ui.components.TagChips
import com.example.wardrobe.ui.components.TagUiModel
import com.example.wardrobe.viewmodel.WardrobeViewModel
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ItemDetailScreen(
    vm: WardrobeViewModel,
    itemId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val itemData by vm.itemFlow(itemId).collectAsState(initial = null)
    val uiState by vm.uiState.collectAsState()
    var showConfirm by remember { mutableStateOf(false) }

    val captureController = rememberCaptureController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Share button
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                val imageBitmapDeferred = captureController.captureAsync()
                                val imageBitmap = imageBitmapDeferred.await()
                                val bitmap = imageBitmap.asAndroidBitmap()
                                shareBitmap(context, bitmap)
                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showConfirm = true }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            )
        }
    ) { padding ->
        if (itemData == null) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Text("Loading...")
            }
        } else {
            val item = itemData!!.item
            val tags = itemData!!.tags
            val tagModels = tags.map { TagUiModel(it.tagId, it.name) }
            val locationName = if (item.stored) {
                item.locationId?.let { locId ->
                    uiState.locations.find { it.locationId == locId }?.name
                }
            } else {
                null
            }
            val createdText = android.text.format.DateFormat
                .format("yyyy-MM-dd", item.createdAt)
                .toString()
            val ownerName = uiState.memberName.takeIf { it.isNotBlank() }

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .capturable(captureController),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ItemSharePoster(
                    description = item.description,
                    createdText = createdText,
                    imageUriString = item.imageUri,
                    tags = tagModels,
                    locationName = locationName,
                    ownerName = ownerName
                )
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Confirm Deletion") },
            text = {
                Text("Are you sure you want to delete this item? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteItem(itemId)
                    showConfirm = false
                    onBack() // Go back to home
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ItemSharePoster(
    description: String,
    createdText: String,
    imageUriString: String?,
    tags: List<TagUiModel>,
    locationName: String?,
    ownerName: String?
) {
    val imageUri = imageUriString?.toUri()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Lucas Wardrobe",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = createdText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (ownerName != null) {
                    Text(
                        text = ownerName,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.titleLarge
            )

            if (!locationName.isNullOrBlank()) {
                Text(
                    text = "Stored in: $locationName",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(4.dp))
                TagChips(
                    tags = tags,
                    selectedIds = tags.map { it.id }.toSet(),
                    onToggle = {},
                    modifier = Modifier.fillMaxWidth(),
                    showCount = false
                )
            }
        }
    }
}

private fun shareBitmap(context: Context, bitmap: Bitmap) {
    val cachePath = File(context.cacheDir, "images")
    cachePath.mkdirs()

    val file = File(cachePath, "image.png")
    val fOut = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
    fOut.flush()
    fOut.close()

    val uri = FileProvider.getUriForFile(
        context,
        "com.example.wardrobe.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share via"))
}
