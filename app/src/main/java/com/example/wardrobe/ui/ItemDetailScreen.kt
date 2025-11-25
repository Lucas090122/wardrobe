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
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.wardrobe.R
import com.example.wardrobe.data.Member
import com.example.wardrobe.data.Season
import com.example.wardrobe.ui.components.TagChips
import com.example.wardrobe.ui.components.TagUiModel
import com.example.wardrobe.viewmodel.WardrobeViewModel
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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
    var showTransferDialog by remember { mutableStateOf(false) }
    var showConfirmTransferDialog by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    var selectedMember by remember { mutableStateOf<Member?>(null) }

    val captureController = rememberCaptureController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_item_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showTransferDialog = true }) {
                        Icon(Icons.Default.SwapHoriz, stringResource(R.string.action_transfer))
                    }
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                val bitmap = captureController.captureAsync().await().asAndroidBitmap()
                                shareBitmap(context, bitmap)
                            } catch (_: Throwable) {}
                        }
                    }) {
                        Icon(Icons.Default.Share, stringResource(R.string.action_share))
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, stringResource(R.string.action_edit))
                    }
                    IconButton(onClick = { showConfirm = true }) {
                        Icon(Icons.Default.Delete, stringResource(R.string.action_delete))
                    }
                }
            )
        }
    ) { padding ->
        if (itemData == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.loading))
            }
        } else {
            val item = itemData!!.item
            val tags = itemData!!.tags
            val tagModels = tags.map { TagUiModel(it.tagId, it.name) }

            val locationName = if (item.stored) {
                item.locationId?.let { id -> uiState.locations.find { it.locationId == id }?.name }
            } else null

            val createdText = android.text.format.DateFormat.format("yyyy-MM-dd", item.createdAt).toString()
            val owner = uiState.members.find { it.memberId == item.ownerMemberId }
            val isMinorOwner = (owner?.age ?: 0) in 0 until 18

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
                    isStored = item.stored,
                    locationName = locationName,
                    ownerName = owner?.name,
                    season = item.season,
                    sizeLabel = item.sizeLabel,
                    showSize = isMinorOwner
                )
            }
        }
    }

    /* ---------- Delete Dialog ---------- */
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteItem(itemId)
                    showConfirm = false
                    onBack()
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    /* ---------- Transfer Dialog ---------- */
    if (showTransferDialog) {
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text(stringResource(R.string.dialog_transfer_title)) },
            text = {
                itemData?.let { data ->
                    val currentOwnerId = data.item.ownerMemberId
                    val otherMembers = uiState.members.filter { it.memberId != currentOwnerId }

                    Column {
                        Text(stringResource(R.string.dialog_transfer_select_member))
                        Spacer(Modifier.height(16.dp))

                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = selectedMember?.name ?: stringResource(R.string.dialog_transfer_placeholder),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                otherMembers.forEach { member ->
                                    DropdownMenuItem(
                                        text = { Text(member.name) },
                                        onClick = {
                                            selectedMember = member
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } ?: Text(stringResource(R.string.loading))
            },
            confirmButton = {
                TextButton(
                    onClick = { showConfirmTransferDialog = true },
                    enabled = selectedMember != null
                ) { Text(stringResource(R.string.action_transfer)) }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    /* ---------- Confirm Transfer Dialog ---------- */
    if (showConfirmTransferDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmTransferDialog = false },
            title = { Text(stringResource(R.string.dialog_transfer_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.dialog_transfer_confirm_message,
                        selectedMember?.name ?: ""
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedMember?.let {
                        vm.transferItem(itemId, it.memberId)
                        showTransferDialog = false
                        showConfirmTransferDialog = false
                        onBack()
                    }
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmTransferDialog = false }) {
                    Text(stringResource(R.string.cancel))
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
    isStored: Boolean,
    locationName: String?,
    ownerName: String?,
    season: Season,
    sizeLabel: String?,
    showSize: Boolean
) {
    val imageUri = imageUriString?.toUri()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.poster_brand_name), style = MaterialTheme.typography.labelMedium)
                    Text(createdText, style = MaterialTheme.typography.bodySmall)
                }
                if (ownerName != null) {
                    Text(ownerName, style = MaterialTheme.typography.labelLarge)
                }
            }

            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Text(description, style = MaterialTheme.typography.titleLarge)

            Text(
                text = stringResource(R.string.poster_season, season.name.replace("_", "/")),
                style = MaterialTheme.typography.bodyMedium
            )

            if (showSize && !sizeLabel.isNullOrBlank()) {
                Text(
                    stringResource(R.string.poster_size, sizeLabel),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isStored) {
                val text = if (!locationName.isNullOrBlank())
                    stringResource(R.string.poster_stored_in, locationName)
                else
                    stringResource(R.string.poster_stored_in_unprovided)

                Text(text, style = MaterialTheme.typography.bodyMedium)
            }

            if (tags.isNotEmpty()) {
                Text(stringResource(R.string.poster_tags), style = MaterialTheme.typography.titleSmall)
                TagChips(tags = tags, selectedIds = tags.map { it.id }.toSet(), onToggle = {}, showCount = false)
            }
        }
    }
}

private fun shareBitmap(context: Context, bitmap: Bitmap) {
    val cachePath = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(cachePath, "image.png")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_via)))
}
