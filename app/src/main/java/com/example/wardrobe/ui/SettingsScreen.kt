package com.example.wardrobe.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.wardrobe.R
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repo: WardrobeRepository,
    mainVm: MainViewModel
) {
    val scope = rememberCoroutineScope()
    val savedPin by repo.settings.adminPin.collectAsState(initial = null)

    val nfcMode by mainVm.nfcMode
    val pendingTagId by mainVm.pendingTagIdForBinding

    val locations by repo.observeLocations().collectAsState(initial = emptyList())

    var showChangePinDialog by remember { mutableStateOf(false) }

    var selectedLocationId by remember { mutableStateOf<Long?>(null) }
    var selectedLocationName by remember { mutableStateOf("") }
    var locationDropdownExpanded by remember { mutableStateOf(false) }
    var nfcError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pendingTagId, locations) {
        if (pendingTagId != null && locations.isNotEmpty() && selectedLocationId == null) {
            val first = locations.first()
            selectedLocationId = first.locationId
            selectedLocationName = first.name
        }
    }

    val showNfcBindDialog = nfcMode == MainViewModel.NfcMode.BindLocation && pendingTagId != null

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(stringResource(R.string.settings_security), style = MaterialTheme.typography.titleMedium)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = savedPin != null) { showChangePinDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_change_admin_pin), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (savedPin != null)
                                stringResource(R.string.settings_change_admin_pin_desc)
                            else
                                stringResource(R.string.settings_admin_pin_not_set_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (savedPin != null) {
                        Icon(Icons.Default.ArrowForward, stringResource(R.string.settings_change_admin_pin))
                    }
                }
            }

            if (savedPin == null) {
                Text(
                    stringResource(R.string.settings_admin_pin_not_set_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Text(stringResource(R.string.settings_nfc_stickers), style = MaterialTheme.typography.titleMedium)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        nfcError = null
                        selectedLocationId = null
                        selectedLocationName = ""
                        mainVm.startBindLocationMode()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_add_nfc_sticker), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.settings_add_nfc_sticker_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ArrowForward, stringResource(R.string.settings_add_nfc_sticker))
                }
            }

            if (nfcMode == MainViewModel.NfcMode.BindLocation && pendingTagId == null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.settings_nfc_scan_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // -------- Change PIN dialog --------
    if (showChangePinDialog) {
        var currentPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text(stringResource(R.string.settings_change_admin_pin)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { currentPin = it.take(4); pinError = null },
                        label = { Text(stringResource(R.string.settings_current_pin)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it.take(4); pinError = null },
                        label = { Text(stringResource(R.string.settings_new_pin)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it.take(4); pinError = null },
                        label = { Text(stringResource(R.string.settings_confirm_new_pin)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (pinError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(pinError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                val errorNoPin = stringResource(R.string.settings_error_no_pin)
                val errorIncorrectPin = stringResource(R.string.settings_error_incorrect_pin)
                val errorPinLength = stringResource(R.string.settings_error_pin_length)
                val errorPinMismatch = stringResource(R.string.settings_error_pin_mismatch)
                TextButton(onClick = {
                    scope.launch {
                        when {
                            savedPin == null -> pinError = errorNoPin
                            currentPin != savedPin -> pinError = errorIncorrectPin
                            newPin.length < 4 -> pinError = errorPinLength
                            newPin != confirmPin -> pinError = errorPinMismatch
                            else -> {
                                repo.settings.setAdminPin(newPin)
                                showChangePinDialog = false
                            }
                        }
                    }
                }) {
                    Text(stringResource(R.string.change))
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // -------- NFC bind dialog --------
    if (showNfcBindDialog) {
        AlertDialog(
            onDismissRequest = {
                selectedLocationId = null
                selectedLocationName = ""
                nfcError = null
                mainVm.cancelBindLocationMode()
            },
            title = { Text(stringResource(R.string.settings_bind_nfc_sticker)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.settings_nfc_detected, pendingTagId ?: ""),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (locations.isEmpty()) {
                        Text(
                            stringResource(R.string.settings_no_locations_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = locationDropdownExpanded,
                            onExpandedChange = { locationDropdownExpanded = !locationDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                readOnly = true,
                                value = selectedLocationName,
                                onValueChange = {},
                                label = { Text(stringResource(R.string.settings_bind_to_location)) },
                                placeholder = { if (selectedLocationName.isEmpty()) Text(stringResource(R.string.settings_select_location)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationDropdownExpanded) }
                            )

                            ExposedDropdownMenu(
                                expanded = locationDropdownExpanded,
                                onDismissRequest = { locationDropdownExpanded = false }
                            ) {
                                locations.forEach { location ->
                                    DropdownMenuItem(
                                        text = { Text(location.name) },
                                        onClick = {
                                            selectedLocationId = location.locationId
                                            selectedLocationName = location.name
                                            locationDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (nfcError != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(nfcError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                val errorStickerMissing = stringResource(R.string.settings_error_sticker_missing)
                val errorSelectLocation = stringResource(R.string.settings_error_select_location)
                val boundMsg = stringResource(
                    R.string.settings_snackbar_sticker_bound,
                    selectedLocationName.ifBlank { "" }
                )
                /*
                val boundMsg = remember(selectedLocationName) {
                    context.getString(
                        R.string.settings_snackbar_sticker_bound,
                        selectedLocationName.ifBlank { context.getString(R.string.settings_location_unknown) }
                    )
                }*/

                TextButton(onClick = {
                    scope.launch {
                        val tagId = pendingTagId
                        val locId = selectedLocationId

                        if (locations.isEmpty()) return@launch
                        if (tagId == null) {
                            nfcError = errorStickerMissing
                            return@launch
                        }
                        if (locId == null) {
                            nfcError = errorSelectLocation
                            return@launch
                        }

                        selectedLocationId = null
                        selectedLocationName = ""
                        nfcError = null
                        mainVm.onLocationBound()

                        repo.bindNfcTagToLocation(tagId, locId)

                        snackbarHostState.showSnackbar(
                            message = boundMsg
                        )
                    }
                }) {
                    Text(stringResource(R.string.bind))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedLocationId = null
                    selectedLocationName = ""
                    nfcError = null
                    mainVm.cancelBindLocationMode()
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
