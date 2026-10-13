package com.inventraoficial.inventra.data.local

import kotlinx.coroutines.flow.Flow

enum class ThemeMode {
    Light,
    Dark,
    System,
}

interface AppPreferences {
    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
