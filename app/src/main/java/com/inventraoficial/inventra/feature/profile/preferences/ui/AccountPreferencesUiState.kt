package com.inventraoficial.inventra.feature.profile.preferences.ui

import com.inventraoficial.inventra.data.local.ThemeMode

data class AccountPreferencesUiState(
    val theme: ThemeMode = ThemeMode.System,
)

val ThemeMode.label: String
    get() =
        when (this) {
            ThemeMode.Light -> "Claro"
            ThemeMode.Dark -> "Escuro"
            ThemeMode.System -> "Sistema"
        }
