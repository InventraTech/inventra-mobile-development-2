package com.inventraoficial.inventra.feature.notifications.background

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

// Guarda, por usuário, o id do último alerta já avisado no celular, para não avisar o mesmo duas vezes.
interface LastNotifiedAlertStore {
    suspend fun lastNotifiedId(userId: String): Int?

    suspend fun save(
        userId: String,
        alertId: Int,
    )
}

internal val Context.alertNotificationsDataStore by preferencesDataStore(name = "alert_notifications")

class DataStoreLastNotifiedAlertStore(
    private val context: Context,
) : LastNotifiedAlertStore {
    override suspend fun lastNotifiedId(userId: String): Int? =
        context.alertNotificationsDataStore.data
            .first()[key(userId)]

    override suspend fun save(
        userId: String,
        alertId: Int,
    ) {
        context.alertNotificationsDataStore.edit { preferences -> preferences[key(userId)] = alertId }
    }

    private fun key(userId: String) = intPreferencesKey("last_alert_$userId")
}
