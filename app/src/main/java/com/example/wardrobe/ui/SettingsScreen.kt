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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * Settings screen for the app.
 *
 * Sections:
 *  - Security: change Admin PIN
 *  - NFC Storage Stickers: bind NFC stickers to Locations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repo: WardrobeRepository,
    mainVm: MainViewModel
) {
    val scope = rememberCoroutineScope()

    // Admin PIN state (null → not set)
    val savedPin by repo.settings.adminPin.collectAsState(initial = null)

    // NFC mode and scanned tag ID
    val nfcMode by mainVm.nfcMode
    val pendingTagId by mainVm.pendingTagIdForBinding

    // All locations for NFC binding
    val locations by repo.observeLocations().collectAsState(initial = emptyList())

    var showChangePinDialog by remember { mutableStateOf(false) }

    // Local UI state for NFC dropdown (used inside dialog)
    var selectedLocationId by remember { mutableStateOf<Long?>(null) }
    var selectedLocationName by remember { mutableStateOf("") }
    var locationDropdownExpanded by remember { mutableStateOf(false) }
    var nfcError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto pre-select first location when a new tag is detected
    LaunchedEffect(pendingTagId, locations) {
        if (pendingTagId != null && locations.isNotEmpty() && selectedLocationId == null) {
            val first = locations.first()
            selectedLocationId = first.locationId
            selectedLocationName = first.name
        }
    }

    // Show NFC binding dialog only when:
    //  - We are in BindLocation mode
    //  - A tag has been detected
    val showNfcBindDialog =
        nfcMode == MainViewModel.NfcMode.BindLocation && pendingTagId != null

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
            // ---------------- Security ----------------
            Text(
                text = "Security",
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = savedPin != null) {
                        showChangePinDialog = true
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Change Admin PIN",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (savedPin != null) {
                                "Update the existing 4-digit Admin PIN."
                            } else {
                                "Admin PIN is not set yet. Enable Admin Mode from the drawer first."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (savedPin != null) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Change PIN"
                        )
                    }
                }
            }

            if (savedPin == null) {
                Text(
                    text = "No Admin PIN set. Turn on Admin Mode in the drawer to set one.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // ---------------- NFC Storage Stickers ----------------
            Text(
                text = "NFC Storage Stickers",
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Enter binding mode and wait for a tag scan
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
                        Text(
                            text = "Add new NFC sticker",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Scan a new sticker and bind it to a storage location.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Add NFC sticker"
                    )
                }
            }

            // Small hint below card when waiting for a scan
            if (nfcMode == MainViewModel.NfcMode.BindLocation && pendingTagId == null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Hold a new NFC sticker near your phone to register it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // --------------- Change Admin PIN dialog ---------------
    if (showChangePinDialog) {
        var currentPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Change Admin PIN") },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { v ->
                            currentPin = v.take(4)
                            pinError = null
                        },
                        label = { Text("Current PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { v ->
                            newPin = v.take(4)
                            pinError = null
                        },
                        label = { Text("New PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { v ->
                            confirmPin = v.take(4)
                            pinError = null
                        },
                        label = { Text("Confirm New PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (pinError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = pinError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        when {
                            savedPin == null -> {
                                pinError = "No existing PIN set"
                            }
                            currentPin != savedPin -> {
                                pinError = "Current PIN is incorrect"
                            }
                            newPin.length < 4 -> {
                                pinError = "New PIN must be 4 digits"
                            }
                            newPin != confirmPin -> {
                                pinError = "New PINs do not match"
                            }
                            else -> {
                                repo.settings.setAdminPin(newPin)
                                showChangePinDialog = false
                            }
                        }
                    }
                }) {
                    Text("Change")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --------------- NFC bind dialog ---------------
    if (showNfcBindDialog) {
        AlertDialog(
            onDismissRequest = {
                // User closes dialog → leave bind mode
                selectedLocationId = null
                selectedLocationName = ""
                nfcError = null
                mainVm.cancelBindLocationMode()
            },
            title = { Text("Bind NFC sticker") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sticker detected: ${pendingTagId ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (locations.isEmpty()) {
                        Text(
                            text = "No locations available. Please create at least one storage location first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = locationDropdownExpanded,
                            onExpandedChange = { locationDropdownExpanded = !locationDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                readOnly = true,
                                value = selectedLocationName,
                                onValueChange = {},
                                label = { Text("Bind to Location") },
                                placeholder = {
                                    if (selectedLocationName.isEmpty()) {
                                        Text("Select a location")
                                    }
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = locationDropdownExpanded
                                    )
                                }
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
                            Text(
                                text = nfcError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val tagId = pendingTagId
                            val locId = selectedLocationId

                            if (locations.isEmpty()) return@launch

                            if (tagId == null) {
                                nfcError = "Sticker is no longer detected, please scan again."
                                return@launch
                            }
                            if (locId == null) {
                                nfcError = "Please select a location first."
                                return@launch
                            }

                            // Reset local UI + leave bind mode
                            selectedLocationId = null
                            selectedLocationName = ""
                            nfcError = null
                            mainVm.onLocationBound()

                            // Persist tag → location binding
                            scope.launch {
                                repo.bindNfcTagToLocation(tagId, locId)

                                snackbarHostState.showSnackbar(
                                    message = "Sticker bound to ${selectedLocationName.ifBlank { "location" }}",
                                )
                            }
                        }
                    }
                ) {
                    Text("Bind")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedLocationId = null
                        selectedLocationName = ""
                        nfcError = null
                        mainVm.cancelBindLocationMode()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}