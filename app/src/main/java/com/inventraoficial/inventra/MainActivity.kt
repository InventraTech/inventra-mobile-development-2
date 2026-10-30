package com.inventraoficial.inventra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.inventraoficial.inventra.data.local.ThemeMode
import com.inventraoficial.inventra.feature.suppliers.create.LocalSupplierSelection
import com.inventraoficial.inventra.ui.event.LocalClickTracker
import com.inventraoficial.inventra.ui.navigation.SetupNavigation
import com.inventraoficial.inventra.ui.theme.InventraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as InventraApplication).container
        val appPreferences = container.appPreferences
        // Veio do toque numa notificação: abre a tela de Notificações (só na criação, não ao girar a tela).
        val openNotifications =
            savedInstanceState == null && intent.getBooleanExtra(EXTRA_OPEN_NOTIFICATIONS, false)
        setContent {
            val themeMode by appPreferences.themeMode.collectAsState(initial = ThemeMode.System)
            val darkTheme =
                when (themeMode) {
                    ThemeMode.Light -> false
                    ThemeMode.Dark -> true
                    ThemeMode.System -> isSystemInDarkTheme()
                }
            CompositionLocalProvider(
                LocalClickTracker provides container.clickTracker,
                LocalSupplierSelection provides container.supplierSelection,
            ) {
                InventraTheme(darkTheme = darkTheme) {
                    SetupNavigation(openNotifications = openNotifications)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            (application as InventraApplication).container.clickTracker.send()
        }
    }

    companion object {
        const val EXTRA_OPEN_NOTIFICATIONS = "open_notifications"
    }
}
