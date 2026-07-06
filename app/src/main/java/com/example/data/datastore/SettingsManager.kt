package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "screenpulse_settings")

class SettingsManager(private val context: Context) {

    companion object {
        private val KEY_LAST_UNPLUGGED_TIME = longPreferencesKey("last_unplugged_time")
        private val KEY_LAST_UNPLUGGED_BATTERY = intPreferencesKey("last_unplugged_battery")
        private val KEY_LAST_CHARGE_TIME = longPreferencesKey("last_charge_time")
        private val KEY_SCREEN_OFF_TIME_ACCUMULATED = longPreferencesKey("screen_off_time_accumulated")
        private val KEY_UNLOCK_COUNT = intPreferencesKey("unlock_count")
        private val KEY_SCREEN_OFF_START_TIME = longPreferencesKey("screen_off_start_time")
        private val KEY_DEMO_MODE = booleanPreferencesKey("demo_mode")
    }

    val demoMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEMO_MODE] ?: false
    }

    suspend fun setDemoMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DEMO_MODE] = enabled
        }
    }

    val lastUnpluggedTime: Flow<Long> = context.dataStore.data.map { preferences ->
        // Default to a reasonable value (e.g., 4 hours ago) if not set yet
        preferences[KEY_LAST_UNPLUGGED_TIME] ?: (System.currentTimeMillis() - 4 * 3600 * 1000)
    }

    val lastUnpluggedBattery: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_UNPLUGGED_BATTERY] ?: 100
    }

    val lastChargeTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_CHARGE_TIME] ?: (System.currentTimeMillis() - 12 * 3600 * 1000)
    }

    val screenOffTimeAccumulated: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_SCREEN_OFF_TIME_ACCUMULATED] ?: 0L
    }

    val unlockCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_UNLOCK_COUNT] ?: 0
    }

    val screenOffStartTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_SCREEN_OFF_START_TIME] ?: 0L
    }

    suspend fun saveUnpluggedState(timeMs: Long, batteryLevel: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_UNPLUGGED_TIME] = timeMs
            preferences[KEY_LAST_UNPLUGGED_BATTERY] = batteryLevel
            preferences[KEY_SCREEN_OFF_TIME_ACCUMULATED] = 0L
            preferences[KEY_UNLOCK_COUNT] = 0
            preferences[KEY_SCREEN_OFF_START_TIME] = 0L
        }
    }

    suspend fun saveLastChargeTime(timeMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_CHARGE_TIME] = timeMs
        }
    }

    suspend fun saveScreenOffStartTime(timeMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SCREEN_OFF_START_TIME] = timeMs
        }
    }

    suspend fun accumulateScreenOffTime(durationMs: Long) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_SCREEN_OFF_TIME_ACCUMULATED] ?: 0L
            preferences[KEY_SCREEN_OFF_TIME_ACCUMULATED] = current + durationMs
            preferences[KEY_SCREEN_OFF_START_TIME] = 0L
        }
    }

    suspend fun incrementUnlockCount() {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_UNLOCK_COUNT] ?: 0
            preferences[KEY_UNLOCK_COUNT] = current + 1
        }
    }
}
