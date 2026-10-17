package com.inventraoficial.inventra.data.local

import kotlinx.coroutines.flow.Flow

enum class ThemeMode {
    Light,
    Dark,
    System,
}

interface AppPreferences {
    val themeMode: Flow<ThemeMode>

    // Avisos no celular com o app fechado (alertas novos da cozinha).
    val alertNotificationsEnabled: Flow<Boolean>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setAlertNotificationsEnabled(enabled: Boolean)
}
