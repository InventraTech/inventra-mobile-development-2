package com.inventraoficial.inventra.data.event

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

interface ClickCountStore {
    suspend fun counts(): Map<String, Long>

    suspend fun add(column: String)

    suspend fun subtract(sent: Map<String, Long>)
}

internal val Context.uiClicksDataStore by preferencesDataStore(name = "ui_clicks")

class DataStoreClickCountStore(
    private val dataStore: DataStore<Preferences>,
) : ClickCountStore {
    override suspend fun counts(): Map<String, Long> =
        dataStore.data
            .first()
            .asMap()
            .mapNotNull { (key, value) -> (value as? Long)?.let { key.name to it } }
            .toMap()

    override suspend fun add(column: String) {
        dataStore.edit { preferences ->
            val key = longPreferencesKey(column)
            preferences[key] = (preferences[key] ?: 0) + 1
        }
    }

    override suspend fun subtract(sent: Map<String, Long>) {
        dataStore.edit { preferences ->
            sent.forEach { (column, amount) ->
                val key = longPreferencesKey(column)
                val left = (preferences[key] ?: 0) - amount
                if (left > 0) preferences[key] = left else preferences.remove(key)
            }
        }
    }
}
