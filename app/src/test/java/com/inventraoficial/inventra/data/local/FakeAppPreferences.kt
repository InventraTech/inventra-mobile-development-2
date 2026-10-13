package com.inventraoficial.inventra.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppPreferences(
    initial: ThemeMode = ThemeMode.System,
) : AppPreferences {
    private val _themeMode = MutableStateFlow(initial)
    override val themeMode: Flow<ThemeMode> = _themeMode

    var savedModes = mutableListOf<ThemeMode>()
        private set

    override suspend fun setThemeMode(mode: ThemeMode) {
        savedModes += mode
        _themeMode.value = mode
    }
}
