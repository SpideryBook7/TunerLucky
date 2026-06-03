package com.spiderybook.tunerlucky.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LibraryManager(
    private val context: Context
) {

    companion object {

        private val LIBRARY_APPS =
            stringPreferencesKey("library_apps")

        private val FAVORITES =
            stringPreferencesKey("favorite_apps")

        private val GAME_PROFILES =
            stringPreferencesKey("game_profiles")

        private val LAST_PLAYED =
            stringPreferencesKey("last_played")

        private val LOGS =
            stringPreferencesKey("logs")

        val FPS_COUNTER =
            booleanPreferencesKey("fps_counter")

        val AUTO_DETECTION =
            booleanPreferencesKey("auto_detection")

        val AUTO_OVERLAY =
            booleanPreferencesKey("auto_overlay")
    }

    val libraryApps: Flow<List<String>>
        get() = context.dataStore.data.map { preferences ->

            val value =
                preferences[LIBRARY_APPS] ?: ""

            if (value.isBlank()) {
                emptyList()
            } else {
                value.split("|")
            }
        }

    val favoriteApps: Flow<Set<String>>
        get() = context.dataStore.data.map { preferences ->
            preferences[FAVORITES].toPackageList().toSet()
        }

    val gameProfiles: Flow<Map<String, PerformanceProfile>>
        get() = context.dataStore.data.map { preferences ->
            preferences[GAME_PROFILES].toPackageMap().mapValues { entry ->
                runCatching { PerformanceProfile.valueOf(entry.value) }
                    .getOrDefault(PerformanceProfile.BALANCED)
            }
        }

    val lastPlayed: Flow<Map<String, Long>>
        get() = context.dataStore.data.map { preferences ->
            preferences[LAST_PLAYED].toPackageMap().mapValues { entry ->
                entry.value.toLongOrNull() ?: 0L
            }
        }

    val logs: Flow<List<String>>
        get() = context.dataStore.data.map { preferences ->
            preferences[LOGS].toLogList()
        }

    val fpsCounter: Flow<Boolean>
        get() = context.dataStore.data.map { it[FPS_COUNTER] ?: true }

    val autoDetection: Flow<Boolean>
        get() = context.dataStore.data.map { it[AUTO_DETECTION] ?: false }

    val autoOverlay: Flow<Boolean>
        get() = context.dataStore.data.map { it[AUTO_OVERLAY] ?: true }

    suspend fun addApp(
        packageName: String
    ) {

        context.dataStore.edit { preferences ->

            val current =
                preferences[LIBRARY_APPS]
                    ?.split("|")
                    ?.toMutableSet()
                    ?: mutableSetOf()

            current.add(packageName)

            preferences[LIBRARY_APPS] =
                current.joinToString("|")
        }
    }

    suspend fun removeApp(
        packageName: String
    ) {

        context.dataStore.edit { preferences ->

            val current =
                preferences[LIBRARY_APPS]
                    ?.split("|")
                    ?.toMutableSet()
                    ?: mutableSetOf()

            current.remove(packageName)

            preferences[LIBRARY_APPS] =
                current.joinToString("|")

            preferences[FAVORITES] =
                preferences[FAVORITES].toPackageList()
                    .filterNot { it == packageName }
                    .joinToString("|")

            preferences[GAME_PROFILES] =
                preferences[GAME_PROFILES].toPackageMap()
                    .filterKeys { it != packageName }
                    .toEncodedMap()

            preferences[LAST_PLAYED] =
                preferences[LAST_PLAYED].toPackageMap()
                    .filterKeys { it != packageName }
                    .toEncodedMap()
        }
    }

    suspend fun toggleFavorite(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITES].toPackageList().toMutableSet()
            if (!current.add(packageName)) {
                current.remove(packageName)
            }
            preferences[FAVORITES] = current.joinToString("|")
        }
    }

    suspend fun setProfile(packageName: String, profile: PerformanceProfile) {
        context.dataStore.edit { preferences ->
            val current = preferences[GAME_PROFILES].toPackageMap().toMutableMap()
            current[packageName] = profile.name
            preferences[GAME_PROFILES] = current.toEncodedMap()
        }
    }

    suspend fun markPlayed(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[LAST_PLAYED].toPackageMap().toMutableMap()
            current[packageName] = System.currentTimeMillis().toString()
            preferences[LAST_PLAYED] = current.toEncodedMap()
        }
    }

    suspend fun setFpsCounter(enabled: Boolean) {
        context.dataStore.edit { it[FPS_COUNTER] = enabled }
    }

    suspend fun setAutoDetection(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_DETECTION] = enabled }
    }

    suspend fun setAutoOverlay(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_OVERLAY] = enabled }
    }

    suspend fun addLog(message: String) {
        context.dataStore.edit { preferences ->
            val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date())
            val next = (listOf("$time  $message") + preferences[LOGS].toLogList()).take(80)
            preferences[LOGS] = next.joinToString("\n")
        }
    }

    suspend fun clearLibrary() {

        context.dataStore.edit { preferences ->

            preferences.remove(
                LIBRARY_APPS
            )
        }
    }

    private fun String?.toPackageList(): List<String> =
        this?.split("|")?.filter { it.isNotBlank() } ?: emptyList()

    private fun String?.toLogList(): List<String> =
        this?.lines()?.filter { it.isNotBlank() } ?: emptyList()

    private fun String?.toPackageMap(): Map<String, String> =
        toPackageList().mapNotNull { item ->
            val separator = item.indexOf("=")
            if (separator <= 0) {
                null
            } else {
                item.substring(0, separator) to item.substring(separator + 1)
            }
        }.toMap()

    private fun Map<String, String>.toEncodedMap(): String =
        entries.joinToString("|") { "${it.key}=${it.value}" }
}
