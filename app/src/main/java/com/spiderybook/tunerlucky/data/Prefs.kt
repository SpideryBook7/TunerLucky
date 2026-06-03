package com.spiderybook.tunerlucky.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class Prefs(private val context: Context) {
    companion object {
        val WIFI_OPTIMIZATION = booleanPreferencesKey("wifi_optimization")
        val GAMING_MODE = booleanPreferencesKey("gaming_mode")
    }

    val wifiOptimization: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[WIFI_OPTIMIZATION] ?: false }

    suspend fun setWifiOptimization(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WIFI_OPTIMIZATION] = enabled
        }
    }

    val gamingMode: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[GAMING_MODE] ?: false }

    suspend fun setGamingMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[GAMING_MODE] = enabled
        }
    }
}
