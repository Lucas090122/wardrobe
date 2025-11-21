package com.example.wardrobe.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobe.Screen
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // ---------------------------------------------------------------------
    // Navigation state
    // ---------------------------------------------------------------------

    private val _currentScreen: MutableState<Screen> =
        mutableStateOf(Screen.DrawerScreen.Home)

    val currentScreen: MutableState<Screen>
        get() = _currentScreen

    fun setCurrentScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    // ---------------------------------------------------------------------
    // NFC mode management
    // ---------------------------------------------------------------------

    /**
     * NFC interaction modes:
     *  - Idle: normal app behavior; scanning a tag should attempt to navigate.
     *  - BindLocation: Settings is waiting for a tag to be scanned for binding.
     */
    enum class NfcMode {
        Idle,
        BindLocation
    }

    /** Current NFC mode. */
    var nfcMode = mutableStateOf(NfcMode.Idle)
        private set

    /**
     * When in BindLocation mode, the scanned tag ID is stored here
     * until SettingsScreen reads it and binds it to a Location.
     */
    var pendingTagIdForBinding = mutableStateOf<String?>(null)
        private set

    /**
     * When scanning a known tag outside binding mode,
     * MainViewModel sets a navigation request.
     *
     * The UI layer (NavHost) will observe this value,
     * perform navigation, and then call clearNavigationRequest().
     */
    var navigateToLocationId = mutableStateOf<Long?>(null)
        private set

    // ---------------------------------------------------------------------
    // Public NFC functions
    // ---------------------------------------------------------------------

    /** Enter “Add new NFC sticker” mode from Settings screen. */
    fun startBindLocationMode() {
        nfcMode.value = NfcMode.BindLocation
        pendingTagIdForBinding.value = null
    }

    /** Leave binding mode without saving anything. */
    fun cancelBindLocationMode() {
        nfcMode.value = NfcMode.Idle
        pendingTagIdForBinding.value = null
    }

    /**
     * Called by MainActivity when an NFC tag is scanned.
     *
     * Behavior:
     *  - If in BindLocation mode → store tagId and wait for user to choose Location.
     *  - If in Idle mode → request navigation to the tag’s location (Settings will resolve this).
     */
    fun onTagScanned(tagId: String, resolveLocation: suspend (String) -> Long?) {
        viewModelScope.launch {
            when (nfcMode.value) {

                NfcMode.BindLocation -> {
                    // SettingsScreen will immediately see this and show the location picker
                    pendingTagIdForBinding.value = tagId
                }

                NfcMode.Idle -> {
                    // Lookup associated location via repository passed from activity
                    val locationId = resolveLocation(tagId)
                    if (locationId != null) {
                        navigateToLocationId.value = locationId
                    }
                }
            }
        }
    }

    /** Must be called by NavHost after performing navigation. */
    fun clearNavigationRequest() {
        navigateToLocationId.value = null
    }

    /** Called after SettingsScreen finishes binding. */
    fun onLocationBound() {
        cancelBindLocationMode()
    }
}