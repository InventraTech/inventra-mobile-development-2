package com.inventraoficial.inventra.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppPreferences(
    initial: ThemeMode = ThemeMode.System,
    alertNotifications: Boolean = true,
) : AppPreferences {
    private val _themeMode = MutableStateFlow(initial)
    override val themeMode: Flow<ThemeMode> = _themeMode

    private val _alertNotificationsEnabled = MutableStateFlow(alertNotifications)
    override val alertNotificationsEnabled: Flow<Boolean> = _alertNotificationsEnabled

    var savedModes = mutableListOf<ThemeMode>()
        private set

    override suspend fun setThemeMode(mode: ThemeMode) {
        savedModes += mode
        _themeMode.value = mode
    }

    override suspend fun setAlertNotificationsEnabled(enabled: Boolean) {
        _alertNotificationsEnabled.value = enabled
    }
}
