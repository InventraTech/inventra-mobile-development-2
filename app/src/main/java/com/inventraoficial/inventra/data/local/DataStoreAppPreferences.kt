package com.inventraoficial.inventra.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

class DataStoreAppPreferences(
    private val context: Context,
) : AppPreferences {
    override val themeMode: Flow<ThemeMode> =
        context.appPreferencesDataStore.data.map { preferences ->
            preferences[THEME_MODE_KEY]
                ?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } }
                ?: ThemeMode.System
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
}
