package com.example.wardrobe.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.wardrobe.ui.components.TagChips
import com.example.wardrobe.ui.components.TagUiModel
import com.example.wardrobe.viewmodel.WardrobeViewModel
import com.example.wardrobe.util.persistImageToAppStorage
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.unit.min
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    vm: WardrobeViewModel,
    itemId: Long? = null,
    onDone: () -> Unit
) {
    val ui by vm.uiState.collectAsState()
    val allTags = ui.tags

    val editing = if (itemId != null) vm.itemFlow(itemId).collectAsState(initial = null).value else null

    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val selectedIds = remember { mutableStateListOf<Long>() }

    // Pre-fill form
    LaunchedEffect(editing?.item?.itemId, allTags) {
        editing?.let { d ->
            description = d.item.description
            imageUri = d.item.imageUri?.toUri()
            selectedIds.clear(); selectedIds.addAll(d.tags.map { it.tagId })
        }
    }

    val context = LocalContext.current

    // Album picker
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) imageUri = uri }
    )

    // Pre-create output file for camera (in app's private directory /files/images)
    fun newImageFile(): File {
        val dir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
        return File(dir, "${UUID.randomUUID()}.jpg")
    }

    // Camera: Use FileProvider to provide a writable content://, save as file:// directly after success (stable)
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { ok ->
            val file = pendingPhotoFile
            if (ok && file != null) {
                imageUri = file.toUri() // Save as file://, no need to copy again, stable across restarts
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId == null) "Add Item" else "Edit Item") },
                navigationIcon = { TextButton(onClick = onDone) { Text("Back") } },
                actions = {
                    TextButton(onClick = {
                        val finalImageUri = imageUri?.let { uri ->
                            if (uri.scheme == "file") uri
                            else persistImageToAppStorage(context, uri)   // Copy to filesDir/images first
                        }

                        vm.saveItem(
                            itemId = itemId,
                            description = description.trim(),
                            imageUri = finalImageUri?.toString(),
                            tagIds = selectedIds.toList()
                        )
                        onDone()
                    }) { Text("Save") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {

            // --- Image area: tap to select from album --- //
            val maxImageH = 360.dp // or screenH * 0.5f

            var aspect by remember { mutableStateOf(1.6f) } // width / height, give a default value first

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // current available width
                val w = this.maxWidth
                // theoretical height = w / aspect; actual height = min(theoretical height, your max value)
                val desired = w / aspect
                val targetH = min(desired, maxImageH)

                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .width(w)           // explicit width
                            .height(targetH),   // Directly specify the final height
                        contentScale = ContentScale.Fit, // Fit the whole image
                        onSuccess = { s ->
                            val d = s.result.drawable
                            val iw = max(1, d.intrinsicWidth)
                            val ih = max(1, d.intrinsicHeight)
                            aspect = iw.toFloat() / ih  // After updating the real aspect ratio, targetH will be recalculated automatically
                        }
                    )
                } else {
                    // Placeholder when there is no image
                    Box(
                        Modifier
                            .width(w)
                            .height(180.dp)
                            .background(Color(0x11FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) { Text("Tap to select image") }
                }
            }

            Spacer(Modifier.height(8.dp))

            // --- Add a row of "Take Photo" and "Choose from Album" buttons --- //
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    // Prepare output file & content Uri
                    val file = newImageFile().also { pendingPhotoFile = it }
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    takePicture.launch(contentUri)
                }) { Text("Take Photo") }

                OutlinedButton(onClick = {
                    galleryPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) { Text("Choose from Album") }
            }

            Spacer(Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (e.g., Blue down jacket 110cm)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Tag multiple selection
            Spacer(Modifier.height(12.dp))
            Text("Tags")
            Spacer(Modifier.height(8.dp))
            TagChips(
                tags = allTags,
                selectedIds = selectedIds.toSet(),
                onToggle = { id ->
                    if (selectedIds.contains(id)) selectedIds.remove(id)
                    else selectedIds.add(id)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Add new tag UI
            Spacer(Modifier.height(16.dp))
            var newTagName by remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text("New Tag Name") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    val name = newTagName.trim()
                    if (name.isNotEmpty()) {
                        scope.launch {
                            val newId = vm.getOrCreateTag(name)
                            if (newId > 0 && !selectedIds.contains(newId)) {
                                selectedIds.add(newId)
                            }
                            newTagName = "" // Clear input
                        }
                    }
                }) { Text("Add") }
            }
        }
    }
}