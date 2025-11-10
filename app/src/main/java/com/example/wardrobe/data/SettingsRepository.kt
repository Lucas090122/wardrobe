package com.example.wardrobe.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// At the top level of your kotlin file:
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val IS_ADMIN_MODE = booleanPreferencesKey("is_admin_mode")
        val ADMIN_PIN = stringPreferencesKey("admin_pin")
    }

    val isAdminMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.IS_ADMIN_MODE] ?: false
        }

    suspend fun setAdminMode(isAdmin: Boolean) {
        context.dataStore.edit {
            it[Keys.IS_ADMIN_MODE] = isAdmin
        }
    }

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