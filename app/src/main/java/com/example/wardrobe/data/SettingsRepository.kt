package com.example.wardrobe.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ------------------------------------------------------------------
// DataStore setup
// ------------------------------------------------------------------

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// ------------------------------------------------------------------
// Settings repository
// ------------------------------------------------------------------

class SettingsRepository(private val context: Context) {

    // --------------------------------------------------------------
    // Preference keys
    // --------------------------------------------------------------

    private object Keys {
        val IS_ADMIN_MODE = booleanPreferencesKey("is_admin_mode")
        val ADMIN_PIN = stringPreferencesKey("admin_pin")
        val IS_AI_ENABLED = booleanPreferencesKey("is_ai_enabled")
        val CONFIRMED_OUTFIT_DATE = stringPreferencesKey("confirmed_outfit_date")
        val CONFIRMED_OUTFIT_IDS = stringSetPreferencesKey("confirmed_outfit_ids")
    }

    // --------------------------------------------------------------
    // Date helpers (daily outfit confirmation)
    // --------------------------------------------------------------

    private val todayDateString: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

    // --------------------------------------------------------------
    // Daily confirmed outfit
    // --------------------------------------------------------------

    val todaysConfirmedOutfitIds: Flow<Set<Long>?> = context.dataStore.data
        .map { preferences ->
            val savedDate = preferences[Keys.CONFIRMED_OUTFIT_DATE]
            if (savedDate == todayDateString) {
                preferences[Keys.CONFIRMED_OUTFIT_IDS]?.map { it.toLong() }?.toSet()
            } else {
                null
            }
        }

    suspend fun setConfirmedOutfit(items: List<ClothingItem>) {
        context.dataStore.edit {
            it[Keys.CONFIRMED_OUTFIT_DATE] = todayDateString
            it[Keys.CONFIRMED_OUTFIT_IDS] =
                items.map { item -> item.itemId.toString() }.toSet()
        }
    }

    // --------------------------------------------------------------
    // Admin mode
    // --------------------------------------------------------------

    val isAdminMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.IS_ADMIN_MODE] ?: false
        }

    suspend fun setAdminMode(isAdmin: Boolean) {
        context.dataStore.edit {
            it[Keys.IS_ADMIN_MODE] = isAdmin
        }
    }

    // --------------------------------------------------------------
    // AI feature toggle
    // --------------------------------------------------------------

    val isAiEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.IS_AI_ENABLED] ?: false
        }

    suspend fun setAiEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[Keys.IS_AI_ENABLED] = enabled
        }
    }

    // --------------------------------------------------------------
    // Admin PIN
    // --------------------------------------------------------------

    val adminPin: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.ADMIN_PIN]
        }

    suspend fun setAdminPin(pin: String) {
        context.dataStore.edit {
            it[Keys.ADMIN_PIN] = pin
        }
    }
}