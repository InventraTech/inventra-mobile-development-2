package com.inventraoficial.inventra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun InventraPalette.toLightScheme(): ColorScheme =
    lightColorScheme(
        primary = purple,
        onPrimary = InventraOnBrand,
        primaryContainer = InventraBrand,
        onPrimaryContainer = InventraOnBrand,
        secondary = gold,
        tertiary = success,
        error = danger,
        background = background,
        onBackground = ink,
        surface = surface,
        onSurface = ink,
        surfaceVariant = surface,
        onSurfaceVariant = muted,
        outline = muted,
    )

private fun InventraPalette.toDarkScheme(): ColorScheme =
    darkColorScheme(
        primary = purple,
        onPrimary = InventraNavInk,
        primaryContainer = InventraBrand,
        onPrimaryContainer = InventraOnBrand,
        secondary = gold,
        tertiary = success,
        error = danger,
        background = background,
        onBackground = ink,
        surface = surface,
        onSurface = ink,
        surfaceVariant = surface,
        onSurfaceVariant = muted,
        outline = muted,
    )

@Composable
fun InventraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkInventraPalette else LightInventraPalette
    val colorScheme = if (darkTheme) palette.toDarkScheme() else palette.toLightScheme()

    CompositionLocalProvider(LocalInventraPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
