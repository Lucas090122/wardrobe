package com.example.wardrobe.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.wardrobe.data.Location
import com.example.wardrobe.ui.components.TagChips
import com.example.wardrobe.ui.components.TagUiModel
import com.example.wardrobe.viewmodel.WardrobeViewModel
import com.example.wardrobe.util.persistImageToAppStorage
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditItemScreen(
    vm: WardrobeViewModel,
    itemId: Long? = null,
    onDone: () -> Unit
) {
    val ui by vm.uiState.collectAsState()

    val editing = if (itemId != null) vm.itemFlow(itemId).collectAsState(initial = null).value else null

    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val selectedTagIds = remember { mutableStateListOf<Long>() }
    var isStored by remember { mutableStateOf(false) }
    var selectedLocationId by remember { mutableStateOf<Long?>(null) }

    // Pre-fill form
    LaunchedEffect(editing) {
        editing?.let { d ->
            description = d.item.description
            imageUri = d.item.imageUri?.toUri()
            selectedTagIds.clear(); selectedTagIds.addAll(d.tags.map { it.tagId })
            isStored = d.item.stored
            selectedLocationId = d.item.locationId
        }
    }

    val context = LocalContext.current

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) imageUri = uri }
    )

    val newImageFile = remember(context) {
        {
            val dir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
            File(dir, "${UUID.randomUUID()}.jpg")
        }
    }

    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { ok ->
            val file = pendingPhotoFile
            if (ok && file != null) {
                imageUri = file.toUri()
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
                            else persistImageToAppStorage(context, uri)
                        }

                        vm.saveItem(
                            itemId = itemId,
                            description = description.trim(),
                            imageUri = finalImageUri?.toString(),
                            tagIds = selectedTagIds.toList(),
                            stored = isStored,
                            locationId = if (isStored) selectedLocationId else null
                        )
                        onDone()
                    }) { Text("Save") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ImageAndCameraSection(imageUri, galleryPicker, takePicture, newImageFile) { pendingPhotoFile = it }

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
                                modifier = Modifier.fillMaxWidth(),
                                showCount = false
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

            StorageSection(vm, isStored, { isStored = it }, ui.locations, selectedLocationId) { selectedLocationId = it }
        }
    }
}

@Composable
private fun StorageSection(
    vm: WardrobeViewModel,
    isStored: Boolean,
    onStoredChange: (Boolean) -> Unit,
    locations: List<Location>,
    selectedLocationId: Long?,
    onLocationSelected: (Long?) -> Unit
) {
    var newLocationName by remember { mutableStateOf("") }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Stored", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = isStored, onCheckedChange = onStoredChange)
        }

        if (isStored) {
            Spacer(Modifier.height(16.dp))
            Text("Storage Location", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            if (locations.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    locations.forEach { location ->
                        InputChip(
                            selected = location.locationId == selectedLocationId,
                            onClick = { onLocationSelected(location.locationId) },
                            label = { Text(location.name) },
                            trailingIcon = {
                                IconButton(onClick = { vm.deleteLocation(location.locationId) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete location")
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newLocationName,
                onValueChange = { newLocationName = it },
                label = { Text("New location name") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = { vm.addLocation(newLocationName); newLocationName = "" },
                        enabled = newLocationName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add location")
                    }
                }
            )
        }
    }
}

@Composable
private fun ImageAndCameraSection(
    imageUri: Uri?,
    galleryPicker: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>,
    takePicture: androidx.activity.result.ActivityResultLauncher<Uri>,
    newImageFile: () -> File,
    onPendingFile: (File) -> Unit
) {
    val context = LocalContext.current
    var aspect by remember { mutableStateOf(1.6f) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
    ) {
        val w = this.maxWidth
        val targetH = min(w / aspect, 360.dp)

        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.width(w).height(targetH),
                contentScale = ContentScale.Fit,
                onSuccess = { s ->
                    val d = s.result.drawable
                    aspect = max(1, d.intrinsicWidth).toFloat() / max(1, d.intrinsicHeight).toFloat()
                }
            )
        } else {
            Box(
                Modifier.width(w).height(180.dp).background(Color(0x11FFFFFF)),
                contentAlignment = Alignment.Center
            ) { Text("Tap to select image") }
        }
    }

    Spacer(Modifier.height(8.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
            val file = newImageFile().also(onPendingFile)
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            takePicture.launch(contentUri)
        }) { Text("Take Photo") }

        OutlinedButton(onClick = {
            galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) { Text("Choose from Album") }
    }
}
