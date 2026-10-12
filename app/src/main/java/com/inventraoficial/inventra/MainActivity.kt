package com.inventraoficial.inventra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.inventraoficial.inventra.ui.navigation.SetupNavigation
import com.inventraoficial.inventra.ui.theme.InventraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventraTheme {
                SetupNavigation()
            }
        }
    }
}
