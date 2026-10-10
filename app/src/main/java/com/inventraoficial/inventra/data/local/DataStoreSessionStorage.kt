package com.inventraoficial.inventra.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlin.let

private val Context.authDataStore by preferencesDataStore(name = "auth")

class DataStoreSessionStorage(
    private val context: Context,
    private val json: Json,
) : SessionStorage {
    override val token: Flow<String?> =
        context.authDataStore.data.map { preferences -> preferences[TOKEN_KEY] }

    override val user: Flow<SessionUser?> =
        context.authDataStore.data.map { preferences ->
            preferences[USER_KEY]?.let { texto ->
                runCatching { json.decodeFromString<SessionUser>(texto) }.getOrNull()
            }
        }

    override val refreshToken: Flow<String?> =
        context.authDataStore.data.map { preferences -> preferences[REFRESH_TOKEN_KEY] }

    override suspend fun saveSession(
        token: String,
        user: SessionUser,
        refreshToken: String,
    ) {
        context.authDataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_KEY] = json.encodeToString(user)
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    override suspend fun clear() {
        context.authDataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }

    override suspend fun updateUser(user: SessionUser) {
        context.authDataStore.edit { preferences ->
            preferences[USER_KEY] = json.encodeToString(user)
        }
    }

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USER_KEY = stringPreferencesKey("user")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refreshToken")
    }
}
