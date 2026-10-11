package com.inventraoficial.inventra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.inventraoficial.inventra.data.local.ThemeMode
import com.inventraoficial.inventra.ui.navigation.SetupNavigation
import com.inventraoficial.inventra.ui.theme.InventraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appPreferences = (application as InventraApplication).container.appPreferences
        setContent {
            val themeMode by appPreferences.themeMode.collectAsState(initial = ThemeMode.System)
            val darkTheme =
                when (themeMode) {
                    ThemeMode.Light -> false
                    ThemeMode.Dark -> true
                    ThemeMode.System -> isSystemInDarkTheme()
                }
            InventraTheme(darkTheme = darkTheme) {
                SetupNavigation()
            }
        }
    }
}
