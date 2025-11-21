package com.example.wardrobe.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.wardrobe.data.WardrobeRepository
import kotlinx.coroutines.launch

/**
 * Settings screen for the app.
 *
 * Currently focuses on:
 *  - Admin security section
 *  - Allowing user to change Admin PIN (if one is already set)
 *
 * The initial PIN is set when turning on Admin Mode in the drawer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repo: WardrobeRepository
) {
    // Observe the stored Admin PIN (null means no PIN has been set yet)
    val savedPin by repo.settings.adminPin.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var showChangePinDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Security",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = { showChangePinDialog = true },
                // Disable the button if no PIN has been set yet
                enabled = savedPin != null
            ) {
                Text("Change Admin PIN")
            }

            if (savedPin == null) {
                Text(
                    text = "No Admin PIN set yet. Turn on Admin Mode in the drawer to set one.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Dialog for changing the Admin PIN
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
                                // In theory this should never happen, because the Change button
                                // is disabled when there is no existing PIN.
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
}