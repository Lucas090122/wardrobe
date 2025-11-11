//package com.example.wardrobe.ui
//
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.PickVisualMediaRequest
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.min
//import androidx.core.content.FileProvider
//import androidx.core.net.toUri
//import coil.compose.AsyncImage
//import com.example.wardrobe.data.Location
//import com.example.wardrobe.ui.components.TagChips
//import com.example.wardrobe.util.persistImageToAppStorage
//import com.example.wardrobe.viewmodel.DialogEffect
//import com.example.wardrobe.viewmodel.WardrobeViewModel
//import kotlinx.coroutines.launch
//import java.io.File
//import java.util.*
//import kotlin.math.max
//
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
//@Composable
//fun EditItemScreen(
//    vm: WardrobeViewModel,
//    itemId: Long? = null,
//    onDone: () -> Unit
//) {
//    val ui by vm.uiState.collectAsState()
//
//    val editing = if (itemId != null) vm.itemFlow(itemId).collectAsState(initial = null).value else null
//
//    var description by remember { mutableStateOf("") }
//    var imageUri by remember { mutableStateOf<Uri?>(null) }
//    val selectedTagIds = remember { mutableStateListOf<Long>() }
//    var isStored by remember { mutableStateOf(false) }
//    var selectedLocationId by remember { mutableStateOf<Long?>(null) }
//
//    // Pre-fill form
//    LaunchedEffect(editing) {
//        editing?.let { d ->
//            description = d.item.description
//            imageUri = d.item.imageUri?.toUri()
//            selectedTagIds.clear(); selectedTagIds.addAll(d.tags.map { it.tagId })
//            isStored = d.item.stored
//            selectedLocationId = d.item.locationId
//        }
//    }
//
//    val context = LocalContext.current
//
//    val galleryPicker = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.PickVisualMedia(),
//        onResult = { uri -> if (uri != null) imageUri = uri }
//    )
//
//    val newImageFile = remember(context) {
//        {
//            val dir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
//            File(dir, "${UUID.randomUUID()}.jpg")
//        }
//    }
//
//    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
//    val takePicture = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.TakePicture(),
//        onResult = { ok ->
//            val file = pendingPhotoFile
//            if (ok && file != null) {
//                imageUri = file.toUri()
//            }
//        }
//    )
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(if (itemId == null) "Add Item" else "Edit Item") },
//                navigationIcon = { TextButton(onClick = onDone) { Text("Back") } },
//                actions = {
//                    TextButton(onClick = {
//                        val finalImageUri = imageUri?.let { uri ->
//                            if (uri.scheme == "file") uri
//                            else persistImageToAppStorage(context, uri)
//                        }
//
//                        vm.saveItem(
//                            itemId = itemId,
//                            description = description.trim(),
//                            imageUri = finalImageUri?.toString(),
//                            tagIds = selectedTagIds.toList(),
//                            stored = isStored,
//                            locationId = if (isStored) selectedLocationId else null
//                        )
//                        onDone()
//                    }) { Text("Save") }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .fillMaxSize()
//                .verticalScroll(rememberScrollState())
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            ImageAndCameraSection(imageUri, galleryPicker, takePicture, newImageFile) { pendingPhotoFile = it }
//
//            OutlinedTextField(
//                value = description,
//                onValueChange = { description = it },
//                label = { Text("Description (e.g., Blue down jacket 110cm)") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            TagsSection(vm = vm, ui = ui, selectedTagIds = selectedTagIds)
//
//            StorageSection(vm, isStored, { isStored = it }, ui.locations, selectedLocationId) { selectedLocationId = it }
//        }
//    }
//
//    HandleDialogEffects(vm, ui.dialogEffect)
//}
//
//@Composable
//private fun HandleDialogEffects(vm: WardrobeViewModel, effect: DialogEffect) {
//    when (effect) {
//        is DialogEffect.DeleteLocation.AdminConfirm -> {
//            AlertDialog(
//                onDismissRequest = { vm.clearDialogEffect() },
//                title = { Text("Confirm Deletion") },
//                text = { Text("Are you sure you want to delete this location? ${effect.itemCount} items will become unassigned.") },
//                confirmButton = {
//                    TextButton(onClick = { vm.forceDeleteLocation(effect.locationId) }) { Text("Delete") }
//                },
//                dismissButton = {
//                    TextButton(onClick = { vm.clearDialogEffect() }) { Text("Cancel") }
//                }
//            )
//        }
//        is DialogEffect.DeleteLocation.PreventDelete -> {
//            AlertDialog(
//                onDismissRequest = { vm.clearDialogEffect() },
//                title = { Text("Action Not Allowed") },
//                text = { Text("This location is in use by ${effect.itemCount} items and cannot be deleted in Normal Mode.") },
//                confirmButton = {
//                    TextButton(onClick = { vm.clearDialogEffect() }) { Text("OK") }
//                }
//            )
//        }
//        is DialogEffect.DeleteTag.AdminConfirm -> {
//            AlertDialog(
//                onDismissRequest = { vm.clearDialogEffect() },
//                title = { Text("Confirm Deletion") },
//                text = { Text("Are you sure you want to delete this tag? It is used by ${effect.itemCount} items.") },
//                confirmButton = {
//                    TextButton(onClick = { vm.forceDeleteTag(effect.tagId) }) { Text("Delete") }
//                },
//                dismissButton = {
//                    TextButton(onClick = { vm.clearDialogEffect() }) { Text("Cancel") }
//                }
//            )
//        }
//        is DialogEffect.DeleteTag.PreventDelete -> {
//            AlertDialog(
//                onDismissRequest = { vm.clearDialogEffect() },
//                title = { Text("Action Not Allowed") },
//                text = { Text("This tag is in use by ${effect.itemCount} items and cannot be deleted in Normal Mode.") },
//                confirmButton = {
//                    TextButton(onClick = { vm.clearDialogEffect() }) { Text("OK") }
//                }
//            )
//        }
//        else -> {}
//    }
//}
//
//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//private fun TagsSection(vm: WardrobeViewModel, ui: com.example.wardrobe.viewmodel.UiState, selectedTagIds: MutableList<Long>) {
//    var newTagName by remember { mutableStateOf("") }
//    val scope = rememberCoroutineScope()
//
//    Column {
//        Text("Tags", style = MaterialTheme.typography.titleMedium)
//        Spacer(Modifier.height(8.dp))
//        TagChips(
//            tags = ui.tags,
//            selectedIds = selectedTagIds.toSet(),
//            onToggle = { id ->
//                if (selectedTagIds.contains(id)) selectedTagIds.remove(id)
//                else selectedTagIds.add(id)
//            },
//            modifier = Modifier.fillMaxWidth(),
//            showCount = false,
//            onDelete = { vm.deleteTag(it) }
//        )
//        Spacer(Modifier.height(16.dp))
//        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            OutlinedTextField(
//                value = newTagName,
//                onValueChange = { newTagName = it },
//                label = { Text("New Tag Name") },
//                modifier = Modifier.weight(1f)
//            )
//            Button(onClick = {
//                val name = newTagName.trim()
//                if (name.isNotEmpty()) {
//                    scope.launch {
//                        val newId = vm.getOrCreateTag(name)
//                        if (newId > 0 && !selectedTagIds.contains(newId)) {
//                            selectedTagIds.add(newId)
//                        }
//                        newTagName = "" // Clear input
//                    }
//                }
//            }) { Text("Add") }
//        }
//    }
//}
//
//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//private fun StorageSection(
//    vm: WardrobeViewModel,
//    isStored: Boolean,
//    onStoredChange: (Boolean) -> Unit,
//    locations: List<Location>,
//    selectedLocationId: Long?,
//    onLocationSelected: (Long?) -> Unit
//) {
//    var newLocationName by remember { mutableStateOf("") }
//
//    Column {
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Text("Stored", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
//            Switch(checked = isStored, onCheckedChange = onStoredChange)
//        }
//
//        if (isStored) {
//            Spacer(Modifier.height(16.dp))
//            Text("Storage Location", style = MaterialTheme.typography.titleSmall)
//            Spacer(Modifier.height(8.dp))
//
//            if (locations.isNotEmpty()) {
//                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                    locations.forEach { location ->
//                        InputChip(
//                            selected = location.locationId == selectedLocationId,
//                            onClick = {
//                                val newSelection = if (location.locationId == selectedLocationId) null else location.locationId
//                                onLocationSelected(newSelection)
//                            },
//                            label = { Text(location.name) },
//                            trailingIcon = {
//                                Icon(
//                                    imageVector = Icons.Default.Close,
//                                    contentDescription = "Delete location",
//                                    modifier = Modifier
//                                        .size(18.dp)
//                                        .clickable { vm.deleteLocation(location.locationId) })
//                            }
//                        )
//                    }
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                OutlinedTextField(
//                    value = newLocationName,
//                    onValueChange = { newLocationName = it },
//                    label = { Text("New location name") },
//                    modifier = Modifier.weight(1f)
//                )
//                Button(onClick = {
//                    val name = newLocationName.trim()
//                    if (name.isNotEmpty()) {
//                        vm.addLocation(name)
//                        newLocationName = "" // Clear input
//                    }
//                }) { Text("Add") }
//            }
//        }
//    }
//}
//
//@Composable
//private fun ImageAndCameraSection(
//    imageUri: Uri?,
//    galleryPicker: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>,
//    takePicture: androidx.activity.result.ActivityResultLauncher<Uri>,
//    newImageFile: () -> File,
//    onPendingFile: (File) -> Unit
//) {
//    val context = LocalContext.current
//    var aspect by remember { mutableStateOf(1.6f) }
//
//    BoxWithConstraints(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(16.dp))
//            .clickable { galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
//    ) {
//        val w = this.maxWidth
//        val targetH = min(w / aspect, 360.dp)
//
//        if (imageUri != null) {
//            AsyncImage(
//                model = imageUri,
//                contentDescription = null,
//                modifier = Modifier
//                    .width(w)
//                    .height(targetH),
//                contentScale = ContentScale.Fit,
//                onSuccess = { s ->
//                    val d = s.result.drawable
//                    aspect = max(1, d.intrinsicWidth).toFloat() / max(1, d.intrinsicHeight).toFloat()
//                }
//            )
//        } else {
//            Box(
//                Modifier
//                    .width(w)
//                    .height(180.dp)
//                    .background(Color(0x11FFFFFF)),
//                contentAlignment = Alignment.Center
//            ) { Text("Tap to select image") }
//        }
//    }
//
//    Spacer(Modifier.height(8.dp))
//
//    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//        Button(onClick = {
//            val file = newImageFile().also(onPendingFile)
//            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
//            takePicture.launch(contentUri)
//        }) { Text("Take Photo") }
//
//        OutlinedButton(onClick = {
//            galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//        }) { Text("Choose from Album") }
//    }
//}

//2-----
//package com.example.wardrobe.ui
//
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.PickVisualMediaRequest
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.ColorLens
//import androidx.compose.material.icons.filled.Star
//import androidx.compose.material.icons.outlined.StarBorder
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.min
//import androidx.core.content.FileProvider
//import androidx.core.net.toUri
//import coil.compose.AsyncImage
//import com.example.wardrobe.data.Location
//import com.example.wardrobe.ui.components.TagChips
//import com.example.wardrobe.util.persistImageToAppStorage
//import com.example.wardrobe.viewmodel.DialogEffect
//import com.example.wardrobe.viewmodel.WardrobeViewModel
//import kotlinx.coroutines.launch
//import java.io.File
//import java.util.*
//import kotlin.math.max
//
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
//@Composable
//fun EditItemScreen(
//    vm: WardrobeViewModel,
//    itemId: Long? = null,
//    onDone: () -> Unit
//) {
//    val ui by vm.uiState.collectAsState()
//    val editing = if (itemId != null) vm.itemFlow(itemId).collectAsState(initial = null).value else null
//
//    var description by remember { mutableStateOf("") }
//    var imageUri by remember { mutableStateOf<Uri?>(null) }
//    val selectedTagIds = remember { mutableStateListOf<Long>() }
//    var isStored by remember { mutableStateOf(false) }
//    var selectedLocationId by remember { mutableStateOf<Long?>(null) }
//
//    // 推荐字段状态
//    var category by remember { mutableStateOf("TOP") }
//    var warmthLevel by remember { mutableStateOf(3) }
//    val allOccasions = remember { listOf("CASUAL", "SCHOOL", "SPORT", "FORMAL", "WORK") }
//    val occasionSet = remember { mutableStateListOf<String>() }
//    var isWaterproof by remember { mutableStateOf(false) }
//    var colorHex by remember { mutableStateOf("#FFFFFF") }
//    var isFavorite by remember { mutableStateOf(false) }
//
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    // 预填
//    LaunchedEffect(editing) {
//        editing?.let { d ->
//            description = d.item.description
//            imageUri = d.item.imageUri?.toUri()
//            selectedTagIds.clear(); selectedTagIds.addAll(d.tags.map { it.tagId })
//            isStored = d.item.stored
//            selectedLocationId = d.item.locationId
//            category = d.item.category
//            warmthLevel = d.item.warmthLevel.coerceIn(1, 5)
//            occasionSet.clear()
//            occasionSet.addAll(d.item.occasions.split(",").map { it.trim() }.filter { it.isNotEmpty() })
//            isWaterproof = d.item.isWaterproof
//            colorHex = d.item.color.ifBlank { "#FFFFFF" }
//            isFavorite = d.item.isFavorite
//        }
//    }
//
//    // 图片与相机逻辑
//    val galleryPicker = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.PickVisualMedia(),
//        onResult = { uri -> if (uri != null) imageUri = uri }
//    )
//
//    val newImageFile = remember(context) {
//        {
//            val dir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
//            File(dir, "${UUID.randomUUID()}.jpg")
//        }
//    }
//
//    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
//    val takePicture = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.TakePicture(),
//        onResult = { ok ->
//            val file = pendingPhotoFile
//            if (ok && file != null) imageUri = file.toUri()
//        }
//    )
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(if (itemId == null) "Add Item" else "Edit Item") },
//                navigationIcon = { TextButton(onClick = onDone) { Text("Back") } },
//                actions = {
//                    TextButton(onClick = {
//                        val finalImageUri = imageUri?.let { uri ->
//                            if (uri.scheme == "file") uri
//                            else persistImageToAppStorage(context, uri)
//                        }
//                        vm.saveItem(
//                            itemId = itemId,
//                            description = description.trim(),
//                            imageUri = finalImageUri?.toString(),
//                            tagIds = selectedTagIds.toList(),
//                            stored = isStored,
//                            locationId = if (isStored) selectedLocationId else null,
//                            category = category,
//                            warmthLevel = warmthLevel,
//                            occasions = occasionSet.joinToString(","),
//                            isWaterproof = isWaterproof,
//                            color = colorHex.ifBlank { "#FFFFFF" },
//                            isFavorite = isFavorite
//                        )
//                        onDone()
//                    }) { Text("Save") }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .fillMaxSize()
//                .verticalScroll(rememberScrollState())
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            ImageAndCameraSection(imageUri, galleryPicker, takePicture, newImageFile) { pendingPhotoFile = it }
//
//            OutlinedTextField(
//                value = description,
//                onValueChange = { description = it },
//                label = { Text("Description (e.g., Blue down jacket 110cm)") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            // 推荐字段区
//            Text("Recommendation Attributes", style = MaterialTheme.typography.titleMedium)
//
//            // Category
//            val categories = listOf("TOP", "PANTS", "SHOES", "HAT", "JUMPSUIT", "OUTER")
//            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                categories.forEach { c ->
//                    FilterChip(
//                        selected = (category == c),
//                        onClick = {
//                            category = c
//                            syncTagsFromAttributes(vm, selectedTagIds, category, warmthLevel, occasionSet.toSet(), isWaterproof, scope)
//                        },
//                        label = { Text(c) },
//                        leadingIcon = { if (category == c) Icon(Icons.Default.Check, null) }
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(12.dp))
//            Text("Warmth Level: $warmthLevel")
//            Slider(
//                value = warmthLevel.toFloat(),
//                onValueChange = { warmthLevel = it.toInt().coerceIn(1, 5) },
//                onValueChangeFinished = {
//                    syncTagsFromAttributes(vm, selectedTagIds, category, warmthLevel, occasionSet.toSet(), isWaterproof, scope)
//                },
//                valueRange = 1f..5f,
//                steps = 3
//            )
//
//            Spacer(Modifier.height(12.dp))
//            Text("Occasions", style = MaterialTheme.typography.titleSmall)
//            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                allOccasions.forEach { occ ->
//                    val checked = occasionSet.contains(occ)
//                    FilterChip(
//                        selected = checked,
//                        onClick = {
//                            if (checked) occasionSet.remove(occ) else occasionSet.add(occ)
//                            syncTagsFromAttributes(vm, selectedTagIds, category, warmthLevel, occasionSet.toSet(), isWaterproof, scope)
//                        },
//                        label = { Text(occ) },
//                        leadingIcon = { if (checked) Icon(Icons.Default.Check, null) }
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(12.dp))
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text("Waterproof", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
//                Switch(
//                    checked = isWaterproof,
//                    onCheckedChange = {
//                        isWaterproof = it
//                        syncTagsFromAttributes(vm, selectedTagIds, category, warmthLevel, occasionSet.toSet(), isWaterproof, scope)
//                    }
//                )
//            }
//
//            Spacer(Modifier.height(12.dp))
//            Text("Color", style = MaterialTheme.typography.titleSmall)
//            Spacer(Modifier.height(8.dp))
//            val availableColors = listOf(
//                "#FFFFFF", "#000000", "#FF0000", "#FFA500",
//                "#FFFF00", "#00FF00", "#00FFFF", "#0000FF",
//                "#800080", "#A52A2A"
//            )
//            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                availableColors.forEach { hex ->
//                    val selected = (colorHex.equals(hex, ignoreCase = true))
//                    Box(
//                        modifier = Modifier
//                            .size(36.dp)
//                            .clip(CircleShape)
//                            .background(Color(android.graphics.Color.parseColor(hex)))
//                            .border(
//                                width = if (selected) 3.dp else 1.dp,
//                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
//                                shape = CircleShape
//                            )
//                            .clickable { colorHex = hex }
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(12.dp))
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text("Favorite", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
//                IconToggleButton(checked = isFavorite, onCheckedChange = { isFavorite = it }) {
//                    if (isFavorite) Icon(Icons.Filled.Star, null)
//                    else Icon(Icons.Outlined.StarBorder, null)
//                }
//            }
//
//            Divider()
//
//            TagsSection(vm = vm, ui = ui, selectedTagIds = selectedTagIds)
//            StorageSection(vm, isStored, { isStored = it }, ui.locations, selectedLocationId) { selectedLocationId = it }
//        }
//    }
//
//    HandleDialogEffects(vm, ui.dialogEffect)
//}
//
//private fun syncTagsFromAttributes(
//    vm: WardrobeViewModel,
//    selectedTagIds: MutableList<Long>,
//    category: String,
//    warmthLevel: Int,
//    occasions: Set<String>,
//    isWaterproof: Boolean,
//    scope: kotlinx.coroutines.CoroutineScope
//) {
//    val tagNames = mutableSetOf<String>()
//    val categoryMap = mapOf(
//        "TOP" to "Top", "PANTS" to "Pants", "SHOES" to "Shoes",
//        "HAT" to "Hat", "JUMPSUIT" to "Jumpsuit", "OUTER" to "Jacket"
//    )
//    categoryMap[category]?.let { tagNames.add(it) }
//    val season = when (warmthLevel) {
//        1, 2 -> "Summer"
//        3, 4 -> "Spring/Autumn"
//        else -> "Winter"
//    }
//    tagNames.add(season)
//    if (isWaterproof) tagNames.add("Waterproof")
//    tagNames.addAll(occasions)
//
//    scope.launch {
//        tagNames.forEach { name ->
//            val id = vm.getOrCreateTag(name)
//            if (id > 0 && !selectedTagIds.contains(id)) selectedTagIds.add(id)
//        }
//    }
//}
//
//@Composable
//private fun HandleDialogEffects(vm: WardrobeViewModel, effect: DialogEffect) {
//    when (effect) {
//        is DialogEffect.DeleteLocation.AdminConfirm -> {
//            AlertDialog(
//                onDismissRequest = { vm.clearDialogEffect() },
//                title = { Text("Confirm Deletion") },
//                text = { Text("Are you sure you want to delete this location? ${effect.itemCount} items will become unassigned.") },
//                confirmButton = {
//                    TextButton(onClick = { vm.forceDeleteLocation(effect.locationId) }) { Text("Delete") }
//                },
//                dismissButton = {
//                    TextButton(onClick = { vm.clearDialogEffect() }) { Text("Cancel") }
//                }
//            )
//        }
//        is DialogEffect.DeleteLocation.PreventDelete -> {
//            AlertDialog(
//                onDismissRequest = { vm.clearDialogEffect() },
//                title = { Text("Action Not Allowed") },
//                text = { Text("This location is in use by ${effect.itemCount} items and cannot be deleted in Normal Mode.") },
//                confirmButton = { TextButton(onClick = { vm.clearDialogEffect() }) { Text("OK") } }
//            )
//        }
//        is DialogEffect.DeleteTag.AdminConfirm -> {
//            AlertDialog(
//                onDismissRequest = { vm.clearDialogEffect() },
//                title = { Text("Confirm Deletion") },
//                text = { Text("Are you sure you want to delete this tag? It is used by ${effect.itemCount} items.") },
//                confirmButton = { TextButton(onClick = { vm.forceDeleteTag(effect.tagId) }) { Text("Delete") } },
//                dismissButton = { TextButton(onClick = { vm.clearDialogEffect() }) { Text("Cancel") } }
//            )
//        }
//        is DialogEffect.DeleteTag.PreventDelete -> {
//            AlertDialog(
//                onDismissRequest = { vm.clearDialogEffect() },
//                title = { Text("Action Not Allowed") },
//                text = { Text("This tag is in use by ${effect.itemCount} items and cannot be deleted in Normal Mode.") },
//                confirmButton = { TextButton(onClick = { vm.clearDialogEffect() }) { Text("OK") } }
//            )
//        }
//        else -> {}
//    }
//}
//
//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//private fun TagsSection(vm: WardrobeViewModel, ui: com.example.wardrobe.viewmodel.UiState, selectedTagIds: MutableList<Long>) {
//    var newTagName by remember { mutableStateOf("") }
//    val scope = rememberCoroutineScope()
//
//    Column {
//        Text("Tags", style = MaterialTheme.typography.titleMedium)
//        Spacer(Modifier.height(8.dp))
//        TagChips(
//            tags = ui.tags,
//            selectedIds = selectedTagIds.toSet(),
//            onToggle = { id ->
//                if (selectedTagIds.contains(id)) selectedTagIds.remove(id)
//                else selectedTagIds.add(id)
//            },
//            modifier = Modifier.fillMaxWidth(),
//            showCount = false,
//            onDelete = { vm.deleteTag(it) }
//        )
//        Spacer(Modifier.height(16.dp))
//        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            OutlinedTextField(
//                value = newTagName,
//                onValueChange = { newTagName = it },
//                label = { Text("New Tag Name") },
//                modifier = Modifier.weight(1f)
//            )
//            Button(onClick = {
//                val name = newTagName.trim()
//                if (name.isNotEmpty()) {
//                    scope.launch {
//                        val newId = vm.getOrCreateTag(name)
//                        if (newId > 0 && !selectedTagIds.contains(newId)) {
//                            selectedTagIds.add(newId)
//                        }
//                        newTagName = ""
//                    }
//                }
//            }) { Text("Add") }
//        }
//    }
//}
//
//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//private fun StorageSection(
//    vm: WardrobeViewModel,
//    isStored: Boolean,
//    onStoredChange: (Boolean) -> Unit,
//    locations: List<Location>,
//    selectedLocationId: Long?,
//    onLocationSelected: (Long?) -> Unit
//) {
//    var newLocationName by remember { mutableStateOf("") }
//
//    Column {
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Text("Stored", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
//            Switch(checked = isStored, onCheckedChange = onStoredChange)
//        }
//
//        if (isStored) {
//            Spacer(Modifier.height(16.dp))
//            Text("Storage Location", style = MaterialTheme.typography.titleSmall)
//            Spacer(Modifier.height(8.dp))
//
//            if (locations.isNotEmpty()) {
//                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                    locations.forEach { location ->
//                        InputChip(
//                            selected = location.locationId == selectedLocationId,
//                            onClick = {
//                                val newSelection = if (location.locationId == selectedLocationId) null else location.locationId
//                                onLocationSelected(newSelection)
//                            },
//                            label = { Text(location.name) },
//                            trailingIcon = {
//                                Icon(
//                                    imageVector = Icons.Default.Close,
//                                    contentDescription = "Delete location",
//                                    modifier = Modifier
//                                        .size(18.dp)
//                                        .clickable { vm.deleteLocation(location.locationId) }
//                                )
//                            }
//                        )
//                    }
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                OutlinedTextField(
//                    value = newLocationName,
//                    onValueChange = { newLocationName = it },
//                    label = { Text("New location name") },
//                    modifier = Modifier.weight(1f)
//                )
//                Button(onClick = {
//                    val name = newLocationName.trim()
//                    if (name.isNotEmpty()) {
//                        vm.addLocation(name)
//                        newLocationName = ""
//                    }
//                }) { Text("Add") }
//            }
//        }
//    }
//}
//
//@Composable
//private fun ImageAndCameraSection(
//    imageUri: Uri?,
//    galleryPicker: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>,
//    takePicture: androidx.activity.result.ActivityResultLauncher<Uri>,
//    newImageFile: () -> File,
//    onPendingFile: (File) -> Unit
//) {
//    val context = LocalContext.current
//    var aspect by remember { mutableStateOf(1.6f) }
//
//    BoxWithConstraints(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(16.dp))
//            .clickable { galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
//    ) {
//        val w = this.maxWidth
//        val targetH = min(w / aspect, 360.dp)
//
//        if (imageUri != null) {
//            AsyncImage(
//                model = imageUri,
//                contentDescription = null,
//                modifier = Modifier
//                    .width(w)
//                    .height(targetH),
//                contentScale = ContentScale.Fit,
//                onSuccess = { s ->
//                    val d = s.result.drawable
//                    aspect = max(1, d.intrinsicWidth).toFloat() / max(1, d.intrinsicHeight).toFloat()
//                }
//            )
//        } else {
//            Box(
//                Modifier
//                    .width(w)
//                    .height(180.dp)
//                    .background(Color(0x11FFFFFF)),
//                contentAlignment = Alignment.Center
//            ) { Text("Tap to select image") }
//        }
//    }
//
//    Spacer(Modifier.height(8.dp))
//    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//        Button(onClick = {
//            val file = newImageFile().also(onPendingFile)
//            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
//            takePicture.launch(contentUri)
//        }) { Text("Take Photo") }
//
//        OutlinedButton(onClick = {
//            galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//        }) { Text("Choose from Album") }
//    }
//}
//

//3-------
package com.example.wardrobe.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
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
import com.example.wardrobe.util.persistImageToAppStorage
import com.example.wardrobe.viewmodel.DialogEffect
import com.example.wardrobe.viewmodel.WardrobeViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.math.max

// 全局保存上一次自动同步的 tagIds
private var lastAutoTagIds: MutableSet<Long> = mutableSetOf()

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

    // 推荐字段状态
    var category by remember { mutableStateOf("TOP") }
    var warmthLevel by remember { mutableStateOf(3) }
    val allOccasions = remember { listOf("CASUAL", "SCHOOL", "SPORT", "FORMAL", "WORK") }
    val occasionSet = remember { mutableStateListOf<String>() }
    var isWaterproof by remember { mutableStateOf(false) }
    var colorHex by remember { mutableStateOf("#FFFFFF") }
    var isFavorite by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 预填数据
    LaunchedEffect(editing) {
        editing?.let { d ->
            description = d.item.description
            imageUri = d.item.imageUri?.toUri()
            selectedTagIds.clear(); selectedTagIds.addAll(d.tags.map { it.tagId })
            isStored = d.item.stored
            selectedLocationId = d.item.locationId
            category = d.item.category
            warmthLevel = d.item.warmthLevel.coerceIn(1, 5)
            occasionSet.clear()
            occasionSet.addAll(d.item.occasions.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            isWaterproof = d.item.isWaterproof
            colorHex = d.item.color.ifBlank { "#FFFFFF" }
            isFavorite = d.item.isFavorite
        }
    }

    // 图片选择逻辑
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
            if (ok && file != null) imageUri = file.toUri()
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
                            locationId = if (isStored) selectedLocationId else null,
                            category = category,
                            warmthLevel = warmthLevel,
                            occasions = occasionSet.joinToString(","),
                            isWaterproof = isWaterproof,
                            color = colorHex.ifBlank { "#FFFFFF" },
                            isFavorite = isFavorite
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

            // 推荐字段区
            Text("Recommendation Attributes", style = MaterialTheme.typography.titleMedium)

            // Category
            val categories = listOf("TOP", "PANTS", "SHOES", "HAT", "JUMPSUIT", "OUTER")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { c ->
                    FilterChip(
                        selected = (category == c),
                        onClick = {
                            category = c
                            syncTagsFromAttributes(vm, selectedTagIds, category, warmthLevel, occasionSet.toSet(), isWaterproof, scope)
                        },
                        label = { Text(c) },
                        leadingIcon = { if (category == c) Icon(Icons.Default.Check, null) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Warmth Level: $warmthLevel")
            Slider(
                value = warmthLevel.toFloat(),
                onValueChange = { warmthLevel = it.toInt().coerceIn(1, 5) },
                onValueChangeFinished = {
                    syncTagsFromAttributes(vm, selectedTagIds, category, warmthLevel, occasionSet.toSet(), isWaterproof, scope)
                },
                valueRange = 1f..5f,
                steps = 3
            )

            Spacer(Modifier.height(12.dp))
            Text("Occasions", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allOccasions.forEach { occ ->
                    val checked = occasionSet.contains(occ)
                    FilterChip(
                        selected = checked,
                        onClick = {
                            if (checked) occasionSet.remove(occ) else occasionSet.add(occ)
                            syncTagsFromAttributes(vm, selectedTagIds, category, warmthLevel, occasionSet.toSet(), isWaterproof, scope)
                        },
                        label = { Text(occ) },
                        leadingIcon = { if (checked) Icon(Icons.Default.Check, null) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Waterproof", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Switch(
                    checked = isWaterproof,
                    onCheckedChange = {
                        isWaterproof = it
                        syncTagsFromAttributes(vm, selectedTagIds, category, warmthLevel, occasionSet.toSet(), isWaterproof, scope)
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Color",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(Modifier.height(6.dp))

            val availableColors = listOf(
                "#FFFFFF", "#000000", "#FF0000", "#FFA500",
                "#FFFF00", "#00FF00", "#00FFFF", "#0000FF",
                "#800080", "#A52A2A"
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
            ) {
                availableColors.forEach { hex ->
                    val selected = colorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(hex)))
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                            .clickable { colorHex = hex }
                    )
                }
            }


            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Favorite", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                IconToggleButton(checked = isFavorite, onCheckedChange = { isFavorite = it }) {
                    if (isFavorite) Icon(Icons.Filled.Star, null)
                    else Icon(Icons.Outlined.StarBorder, null)
                }
            }

            Divider()

            TagsSection(vm = vm, ui = ui, selectedTagIds = selectedTagIds)
            StorageSection(vm, isStored, { isStored = it }, ui.locations, selectedLocationId) { selectedLocationId = it }
        }
    }

    HandleDialogEffects(vm, ui.dialogEffect)
}

// ----------- 智能同步逻辑（更新版） -----------
private fun syncTagsFromAttributes(
    vm: WardrobeViewModel,
    selectedTagIds: MutableList<Long>,
    category: String,
    warmthLevel: Int,
    occasions: Set<String>,
    isWaterproof: Boolean,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val tagNames = mutableSetOf<String>()
    val categoryMap = mapOf(
        "TOP" to "Top", "PANTS" to "Pants", "SHOES" to "Shoes",
        "HAT" to "Hat", "JUMPSUIT" to "Jumpsuit", "OUTER" to "Jacket"
    )
    categoryMap[category]?.let { tagNames.add(it) }

    val season = when (warmthLevel) {
        1, 2 -> "Summer"
        3, 4 -> "Spring/Autumn"
        else -> "Winter"
    }
    tagNames.add(season)
    if (isWaterproof) tagNames.add("Waterproof")
    tagNames.addAll(occasions)

    scope.launch {
        val newAutoTagIds = mutableSetOf<Long>()
        for (name in tagNames) {
            val id = vm.getOrCreateTag(name)
            if (id > 0) newAutoTagIds.add(id)
        }

        // 移除旧自动 tag
        val toRemove = lastAutoTagIds - newAutoTagIds
        selectedTagIds.removeAll(toRemove)

        // 添加新的自动 tag
        val toAdd = newAutoTagIds - selectedTagIds.toSet()
        selectedTagIds.addAll(toAdd)

        // 记录新的自动标签集
        lastAutoTagIds = newAutoTagIds
    }
}

@Composable
private fun HandleDialogEffects(vm: WardrobeViewModel, effect: DialogEffect) {
    when (effect) {
        is DialogEffect.DeleteLocation.AdminConfirm -> {
            AlertDialog(
                onDismissRequest = { vm.clearDialogEffect() },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete this location? ${effect.itemCount} items will become unassigned.") },
                confirmButton = {
                    TextButton(onClick = { vm.forceDeleteLocation(effect.locationId) }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { vm.clearDialogEffect() }) { Text("Cancel") }
                }
            )
        }
        is DialogEffect.DeleteLocation.PreventDelete -> {
            AlertDialog(
                onDismissRequest = { vm.clearDialogEffect() },
                title = { Text("Action Not Allowed") },
                text = { Text("This location is in use by ${effect.itemCount} items and cannot be deleted in Normal Mode.") },
                confirmButton = {
                    TextButton(onClick = { vm.clearDialogEffect() }) { Text("OK") }
                }
            )
        }
        is DialogEffect.DeleteTag.AdminConfirm -> {
            AlertDialog(
                onDismissRequest = { vm.clearDialogEffect() },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete this tag? It is used by ${effect.itemCount} items.") },
                confirmButton = {
                    TextButton(onClick = { vm.forceDeleteTag(effect.tagId) }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { vm.clearDialogEffect() }) { Text("Cancel") }
                }
            )
        }
        is DialogEffect.DeleteTag.PreventDelete -> {
            AlertDialog(
                onDismissRequest = { vm.clearDialogEffect() },
                title = { Text("Action Not Allowed") },
                text = { Text("This tag is in use by ${effect.itemCount} items and cannot be deleted in Normal Mode.") },
                confirmButton = {
                    TextButton(onClick = { vm.clearDialogEffect() }) { Text("OK") }
                }
            )
        }
        else -> {}
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(vm: WardrobeViewModel, ui: com.example.wardrobe.viewmodel.UiState, selectedTagIds: MutableList<Long>) {
    var newTagName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column {
        Text("Tags", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        TagChips(
            tags = ui.tags,
            selectedIds = selectedTagIds.toSet(),
            onToggle = { id ->
                if (selectedTagIds.contains(id)) selectedTagIds.remove(id)
                else selectedTagIds.add(id)
            },
            modifier = Modifier.fillMaxWidth(),
            showCount = false,
            onDelete = { vm.deleteTag(it) }
        )
        Spacer(Modifier.height(16.dp))
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
                        if (newId > 0 && !selectedTagIds.contains(newId)) {
                            selectedTagIds.add(newId)
                        }
                        newTagName = ""
                    }
                }
            }) { Text("Add") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
                            onClick = {
                                val newSelection = if (location.locationId == selectedLocationId) null else location.locationId
                                onLocationSelected(newSelection)
                            },
                            label = { Text(location.name) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete location",
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { vm.deleteLocation(location.locationId) }
                                )
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newLocationName,
                    onValueChange = { newLocationName = it },
                    label = { Text("New location name") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    val name = newLocationName.trim()
                    if (name.isNotEmpty()) {
                        vm.addLocation(name)
                        newLocationName = ""
                    }
                }) { Text("Add") }
            }
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    ) {
        val w = this.maxWidth
        val targetH = min(w / aspect, 360.dp)

        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier
                    .width(w)
                    .height(targetH),
                contentScale = ContentScale.Fit,
                onSuccess = { s ->
                    val d = s.result.drawable
                    aspect = max(1, d.intrinsicWidth).toFloat() / max(1, d.intrinsicHeight).toFloat()
                }
            )
        } else {
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

