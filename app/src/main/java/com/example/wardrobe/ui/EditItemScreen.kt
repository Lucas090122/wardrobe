package com.example.wardrobe.ui

import android.net.Uri
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.wardrobe.data.remote.GeminiAiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.wardrobe.data.Location
import com.example.wardrobe.data.Season
import com.example.wardrobe.ui.components.TagChips
import com.example.wardrobe.util.persistImageToAppStorage
import com.example.wardrobe.viewmodel.DialogEffect
import com.example.wardrobe.viewmodel.WardrobeViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.math.max
import com.example.wardrobe.R
import androidx.core.graphics.toColorInt
import androidx.core.graphics.scale

// Globally remember the last automatically-synced tagIds.
// This allows us to remove previously auto-added tags when new AI suggestions come in.
private var lastAutoTagIds: MutableSet<Long> = mutableSetOf()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditItemScreen(
    vm: WardrobeViewModel,
    itemId: Long? = null,
    onDone: () -> Unit
) {
    // Main UI state from the view model (includes settings such as AI mode)
    val ui by vm.uiState.collectAsState()
    val aiEnabled = ui.isAiEnabled

    // When editing an existing item, observe it; when creating a new one this will be null
    val editing = if (itemId != null) vm.itemFlow(itemId).collectAsState(initial = null).value else null

    // Form state
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val selectedTagIds = remember { mutableStateListOf<Long>() }
    var isStored by remember { mutableStateOf(false) }
    var selectedLocationId by remember { mutableStateOf<Long?>(null) }

    // Recommendation-related fields (category, warmth, occasions, etc.)
    var category by remember { mutableStateOf("TOP") }
    var warmthLevel by remember { mutableIntStateOf(3) }
    val allOccasions = remember { listOf("CASUAL", "SCHOOL", "SPORT", "FORMAL", "WORK") }
    val occasionSet = remember { mutableStateListOf<String>() }
    var isWaterproof by remember { mutableStateOf(false) }
    var colorHex by remember { mutableStateOf("#FFFFFF") }
    var isFavorite by remember { mutableStateOf(false) }
    var season by remember { mutableStateOf(Season.SPRING_AUTUMN) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Age-based logic for size recommendation
    val currentMember = ui.members.find { it.name == ui.memberName }
    val isMinor = (currentMember?.age ?: 0) in 0 until 18
    val showSizeField = isMinor && category in listOf("TOP", "PANTS", "SHOES")
    var sizeText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Pre-fill form when editing an existing item
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
            occasionSet.addAll(
                d.item.occasions.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            )
            isWaterproof = d.item.isWaterproof
            colorHex = d.item.color.ifBlank { "#FFFFFF" }
            isFavorite = d.item.isFavorite
            season = d.item.season

            sizeText = d.item.sizeLabel.orEmpty()
        }
    }

    // Image selection logic: from gallery or camera
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) imageUri = uri }
    )

    // Helper to create a new image file inside app storage for camera photos
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

    // Only analyze with AI when a NEW item is created and AI mode is enabled.
    // For existing items we don't auto-override the user's data.
    LaunchedEffect(imageUri, itemId, aiEnabled) {
        // Skip analysis when editing an existing item.
        if (itemId != null) return@LaunchedEffect

        // If AI mode is turned off in settings, do nothing.
        if (!aiEnabled) return@LaunchedEffect

        val uri = imageUri ?: return@LaunchedEffect

        isAnalyzing = true
        try {
            // 1. Load a reasonably sized Bitmap from the Uri on an IO dispatcher
            val bitmap = withContext(Dispatchers.IO) {
                loadScaledBitmapFromUri(context, uri, 1024)
            } ?: return@LaunchedEffect

            // 2. Call Gemini to analyze clothing attributes
            val result = GeminiAiService.analyzeClothing(bitmap) ?: return@LaunchedEffect

            // 3. Populate form fields with AI result
            category = result.category
            description = result.description

            warmthLevel = result.warmthLevel.coerceIn(1, 5)
            season = when (warmthLevel) {
                1, 2 -> Season.SUMMER
                3, 4 -> Season.SPRING_AUTUMN
                else -> Season.WINTER
            }

            colorHex = result.colorHex

            // Sync recommended tags based on updated attributes
            syncTagsFromAttributes(
                vm = vm,
                selectedTagIds = selectedTagIds,
                category = category,
                warmthLevel = warmthLevel,
                occasions = occasionSet.toSet(),
                isWaterproof = isWaterproof,
                scope = scope
            )

            Log.d("EditItemScreen", "Gemini result: $result")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("EditItemScreen", "Gemini analyze error", e)
        } finally {
            isAnalyzing = false
        }
    }

    val titleText = if (itemId == null) stringResource(R.string.add_item)
    else stringResource(R.string.edit_item)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleText) },
                navigationIcon = { TextButton(onClick = onDone) { Text(stringResource(R.string.back)) } },
                actions = {
                    TextButton(onClick = {
                        // Persist image into app storage if it is not already a file Uri
                        val finalImageUri = imageUri?.let { uri ->
                            if (uri.scheme == "file") uri
                            else persistImageToAppStorage(context, uri)
                        }

                        val finalSizeLabel =
                            if (showSizeField && sizeText.isNotBlank()) sizeText.trim() else null

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
                            sizeLabel = finalSizeLabel,
                            isFavorite = isFavorite,
                            season = season
                        )
                        onDone()
                    }) { Text(stringResource(R.string.save)) }
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
            ImageAndCameraSection(
                imageUri = imageUri,
                galleryPicker = galleryPicker,
                takePicture = takePicture,
                newImageFile = newImageFile,
                onPendingFile = { pendingPhotoFile = it }
            )

            if (isAnalyzing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.ai_analyzing))
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Section: recommendation attributes
            Text(
                stringResource(R.string.recommendation_attributes),
                style = MaterialTheme.typography.titleMedium
            )

            // Category chips (internal codes -> localized labels)
            val categoryLabel = mapOf(
                "TOP" to stringResource(R.string.cat_top),
                "PANTS" to stringResource(R.string.cat_pants),
                "SHOES" to stringResource(R.string.cat_shoes),
                "HAT" to stringResource(R.string.cat_hat)
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categoryLabel.forEach { (value, label) ->
                    FilterChip(
                        selected = (category == value),
                        onClick = {
                            category = value
                            // Re-sync tags whenever the category changes
                            syncTagsFromAttributes(
                                vm,
                                selectedTagIds,
                                category,
                                warmthLevel,
                                occasionSet.toSet(),
                                isWaterproof,
                                scope
                            )
                        },
                        label = { Text(label) },
                        leadingIcon = { if (category == value) Icon(Icons.Default.Check, null) }
                    )
                }
            }

            // Optional size field for minors (children's clothing)
            if (showSizeField) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = sizeText,
                    onValueChange = { sizeText = it },
                    label = { Text(stringResource(R.string.size_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.season), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Season.entries.forEach { s ->
                    FilterChip(
                        selected = (season == s),
                        onClick = { season = s },
                        label = { Text(localizeSeasonLabel(s)) },
                        leadingIcon = { if (season == s) Icon(Icons.Default.Check, null) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.warmth_level, warmthLevel))
            Slider(
                value = warmthLevel.toFloat(),
                onValueChange = {
                    warmthLevel = it.toInt().coerceIn(1, 5)
                    season = when (warmthLevel) {
                        1, 2 -> Season.SUMMER
                        3, 4 -> Season.SPRING_AUTUMN
                        else -> Season.WINTER
                    }
                },
                onValueChangeFinished = {
                    // When the slider is released, sync tags again based on new warmth
                    syncTagsFromAttributes(
                        vm,
                        selectedTagIds,
                        category,
                        warmthLevel,
                        occasionSet.toSet(),
                        isWaterproof,
                        scope
                    )
                },
                valueRange = 1f..5f,
                steps = 3
            )

            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.occasions), style = MaterialTheme.typography.titleSmall)

            val occasionLabel = mapOf(
                "CASUAL" to stringResource(R.string.occ_casual),
                "SCHOOL" to stringResource(R.string.occ_school),
                "SPORT" to stringResource(R.string.occ_sport),
                "FORMAL" to stringResource(R.string.occ_formal),
                "WORK" to stringResource(R.string.occ_work)
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                occasionLabel.forEach { (value, label) ->
                    val checked = occasionSet.contains(value)
                    FilterChip(
                        selected = checked,
                        onClick = {
                            if (checked) occasionSet.remove(value) else occasionSet.add(value)
                            // Occasion also affects tag suggestions
                            syncTagsFromAttributes(
                                vm,
                                selectedTagIds,
                                category,
                                warmthLevel,
                                occasionSet.toSet(),
                                isWaterproof,
                                scope
                            )
                        },
                        label = { Text(label) },
                        leadingIcon = { if (checked) Icon(Icons.Default.Check, null) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.waterproof),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isWaterproof,
                    onCheckedChange = {
                        isWaterproof = it
                        // Waterproof is also encoded as a tag
                        syncTagsFromAttributes(
                            vm,
                            selectedTagIds,
                            category,
                            warmthLevel,
                            occasionSet.toSet(),
                            isWaterproof,
                            scope
                        )
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.color),
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
                            .background(Color(hex.toColorInt()))
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
                Text(
                    stringResource(R.string.favorite),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                IconToggleButton(
                    checked = isFavorite,
                    onCheckedChange = { isFavorite = it }
                ) {
                    if (isFavorite) Icon(Icons.Filled.Star, null)
                    else Icon(Icons.Outlined.StarBorder, null)
                }
            }

            @Suppress("DEPRECATION")
            Divider()

            // Tag selection & manual tag creation
            TagsSection(vm = vm, ui = ui, selectedTagIds = selectedTagIds)

            // Storage information: whether stored, and where
            StorageSection(
                vm = vm,
                isStored = isStored,
                onStoredChange = { isStored = it },
                locations = ui.locations,
                selectedLocationId = selectedLocationId
            ) { selectedLocationId = it }
        }
    }

    // Global dialogs for delete-location / delete-tag confirmation
    HandleDialogEffects(vm, ui.dialogEffect)
}

// ----------- Smart tag sync logic -----------
/**
 * Automatically syncs tags based on clothing attributes (category, warmth, occasions, waterproof).
 * This function:
 *  1. Computes a set of "automatic" tag names from attributes.
 *  2. Ensures those tags exist in DB (creating them if necessary).
 *  3. Removes previously auto-added tags that are no longer relevant.
 *  4. Adds new tags that should now be attached.
 *
 * NOTE: Here we use English-like internal names ("Top", "Waterproof", "CASUAL")
 * as language-independent keys stored in the database. UI localization is done
 * separately when rendering tags.
 */
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

    // Map internal category codes to tag keys
    val categoryMap = mapOf(
        "TOP" to "Top",
        "PANTS" to "Pants",
        "SHOES" to "Shoes",
        "HAT" to "Hat"
    )
    categoryMap[category]?.let { tagNames.add(it) }

    if (isWaterproof) tagNames.add("Waterproof")
    tagNames.addAll(occasions)

    scope.launch {
        val newAutoTagIds = mutableSetOf<Long>()
        for (name in tagNames) {
            val id = vm.getOrCreateTag(name)
            if (id > 0) newAutoTagIds.add(id)
        }

        // Remove auto-tags that were previously added but are no longer desired
        val toRemove = lastAutoTagIds - newAutoTagIds
        selectedTagIds.removeAll(toRemove)

        // Add newly recommended auto-tags that are not in the selection yet
        val toAdd = newAutoTagIds - selectedTagIds.toSet()
        selectedTagIds.addAll(toAdd)

        // Remember the latest set of auto-generated tags for next time
        lastAutoTagIds = newAutoTagIds
    }
}

@Composable
private fun HandleDialogEffects(vm: WardrobeViewModel, effect: DialogEffect) {
    when (effect) {
        is DialogEffect.DeleteLocation.AdminConfirm -> {
            AlertDialog(
                onDismissRequest = { vm.clearDialogEffect() },
                title = { Text(stringResource(R.string.dialog_confirm_delete)) },
                text = {
                    Text(
                        stringResource(
                            R.string.dialog_location_delete_msg,
                            effect.itemCount
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = { vm.forceDeleteLocation(effect.locationId) }) {
                        Text(stringResource(R.string.dialog_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { vm.clearDialogEffect() }) {
                        Text(stringResource(R.string.dialog_cancel))
                    }
                }
            )
        }

        is DialogEffect.DeleteLocation.PreventDelete -> {
            AlertDialog(
                onDismissRequest = { vm.clearDialogEffect() },
                title = { Text(stringResource(R.string.dialog_action_not_allowed)) },
                text = {
                    Text(
                        stringResource(
                            R.string.dialog_location_prevent_delete_msg,
                            effect.itemCount
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = { vm.clearDialogEffect() }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }

        is DialogEffect.DeleteTag.AdminConfirm -> {
            AlertDialog(
                onDismissRequest = { vm.clearDialogEffect() },
                title = { Text(stringResource(R.string.dialog_confirm_delete)) },
                text = {
                    Text(
                        stringResource(
                            R.string.dialog_tag_delete_msg,
                            effect.itemCount
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = { vm.forceDeleteTag(effect.tagId) }) {
                        Text(stringResource(R.string.dialog_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { vm.clearDialogEffect() }) {
                        Text(stringResource(R.string.dialog_cancel))
                    }
                }
            )
        }

        is DialogEffect.DeleteTag.PreventDelete -> {
            AlertDialog(
                onDismissRequest = { vm.clearDialogEffect() },
                title = { Text(stringResource(R.string.dialog_action_not_allowed)) },
                text = {
                    Text(
                        stringResource(
                            R.string.dialog_tag_prevent_delete_msg,
                            effect.itemCount
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = { vm.clearDialogEffect() }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }

        else -> {}
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(
    vm: WardrobeViewModel,
    ui: com.example.wardrobe.viewmodel.UiState,
    selectedTagIds: MutableList<Long>
) {
    var newTagName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column {
        Text(stringResource(R.string.tags), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        // Localize built-in tag names for display, while keeping IDs and keys intact.
        val localizedTags = ui.tags.map { tag ->
            tag.copy(name = localizeTagName(tag.name))
        }

        // Existing tags with chip selection
        TagChips(
            tags = localizedTags,
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

        // Add new custom tag by name (user-defined tags are not localized)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newTagName,
                onValueChange = { newTagName = it },
                label = { Text(stringResource(R.string.new_tag_name)) },
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
            }) { Text(stringResource(R.string.add)) }
        }
    }
}

/**
 * Maps internal tag keys stored in the database to localized labels
 * for display in the UI. User-defined tags fall back to the original name.
 */
@Composable
private fun localizeTagName(raw: String): String {
    return when (raw) {
        // Categories
        "Top"   -> stringResource(R.string.cat_top)
        "Pants" -> stringResource(R.string.cat_pants)
        "Shoes" -> stringResource(R.string.cat_shoes)
        "Hat"   -> stringResource(R.string.cat_hat)

        // Attributes
        "Waterproof" -> stringResource(R.string.waterproof)

        // Occasions (match the internal codes used in syncTagsFromAttributes / occasions)
        "CASUAL" -> stringResource(R.string.occ_casual)
        "SCHOOL" -> stringResource(R.string.occ_school)
        "SPORT"  -> stringResource(R.string.occ_sport)
        "FORMAL" -> stringResource(R.string.occ_formal)
        "WORK"   -> stringResource(R.string.occ_work)

        else -> raw // User-created tags remain as-is
    }
}

/**
 * Maps internal season enum values to localized labels.
 * The Season enum itself is language-independent; only UI strings are localized.
 */
@Composable
private fun localizeSeasonLabel(season: Season): String {
    return when (season) {
        Season.SPRING_AUTUMN -> stringResource(R.string.season_spring_autumn)
        Season.SUMMER        -> stringResource(R.string.season_summer)
        Season.WINTER        -> stringResource(R.string.season_winter)
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
            Text(
                stringResource(R.string.stored),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = isStored, onCheckedChange = onStoredChange)
        }

        if (isStored) {
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.storage_location), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            // Existing locations as selectable chips, each with a delete icon (admin-aware)
            if (locations.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    locations.forEach { location ->
                        InputChip(
                            selected = location.locationId == selectedLocationId,
                            onClick = {
                                val newSelection =
                                    if (location.locationId == selectedLocationId) null
                                    else location.locationId
                                onLocationSelected(newSelection)
                            },
                            label = { Text(location.name) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.delete_location),
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

            // Create a new named storage location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newLocationName,
                    onValueChange = { newLocationName = it },
                    label = { Text(stringResource(R.string.new_location_name)) },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    val name = newLocationName.trim()
                    if (name.isNotEmpty()) {
                        vm.addLocation(name)
                        newLocationName = ""
                    }
                }) { Text(stringResource(R.string.add)) }
            }
        }
    }
}

/**
 * Combined UI for displaying the selected image and providing shortcuts
 * to open the gallery picker or camera.
 */
@Composable
private fun ImageAndCameraSection(
    imageUri: Uri?,
    galleryPicker: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>,
    takePicture: androidx.activity.result.ActivityResultLauncher<Uri>,
    newImageFile: () -> File,
    onPendingFile: (File) -> Unit
) {
    val context = LocalContext.current
    var aspect by remember { mutableFloatStateOf(1.6f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                galleryPicker.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
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
                    // Adjust aspect ratio based on real image size to avoid distortion
                    val d = s.result.drawable
                    aspect = max(1, d.intrinsicWidth).toFloat() /
                            max(1, d.intrinsicHeight).toFloat()
                }
            )
        } else {
            Box(
                Modifier
                    .width(w)
                    .height(180.dp)
                    .background(Color(0x11FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.tap_select_image))
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
            // Create a file, remember it, and launch camera with a FileProvider Uri
            val file = newImageFile().also(onPendingFile)
            val contentUri =
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            takePicture.launch(contentUri)
        }) { Text(stringResource(R.string.take_photo)) }

        OutlinedButton(onClick = {
            galleryPicker.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }) { Text(stringResource(R.string.choose_from_album)) }
    }
}

/**
 * Utility: load a Bitmap from a Uri, optionally downscaling it so that the
 * longest side is at most [maxSize] pixels. This keeps memory and upload
 * size under control.
 */
private fun loadScaledBitmapFromUri(
    context: android.content.Context,
    uri: Uri,
    maxSize: Int
): Bitmap? {
    return try {
        val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        val w = raw.width
        val h = raw.height
        val maxDim = maxOf(w, h)

        if (maxDim <= maxSize) {
            raw
        } else {
            val scale = maxSize.toFloat() / maxDim.toFloat()
            val newW = (w * scale).roundToInt()
            val newH = (h * scale).roundToInt()
            raw.scale(newW, newH)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}