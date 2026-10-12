@file:Suppress("MagicNumber")

package com.inventraoficial.inventra.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val InventraBrand = Color(0xFF4B0F91)
val InventraOnBrand = Color.White
val InventraNavInk = Color(0xFF131018)

private val GoldLight = Color(0xFFF0B400)
private val SuccessLight = Color(0xFF21804C)
private val DangerLight = Color(0xFFC4281C)

private val PurpleDark = Color(0xFFCDB4FF)
private val GoldDark = Color(0xFFFFC93C)
private val SuccessDark = Color(0xFF5BCB8C)
private val DangerDark = Color(0xFFFF7A6E)
private val SurfaceDark = Color(0xFF1E1A26)
private val InkDark = Color(0xFFEDE7F5)
private val MutedDark = Color(0xFFA9A1B6)
private val NavBarDark = Color(0xFF2A2433)

@Immutable
data class InventraPalette(
    val purple: Color,
    val gold: Color,
    val success: Color,
    val danger: Color,
    val background: Color,
    val surface: Color,
    val ink: Color,
    val muted: Color,
    val navBar: Color,
    val isDark: Boolean,
)

val LightInventraPalette =
    InventraPalette(
        purple = InventraBrand,
        gold = GoldLight,
        success = SuccessLight,
        danger = DangerLight,
        background = Color.White,
        surface = Color.White,
        ink = InventraNavInk,
        muted = Color.Gray,
        navBar = InventraNavInk,
        isDark = false,
    )

val DarkInventraPalette =
    InventraPalette(
        purple = PurpleDark,
        gold = GoldDark,
        success = SuccessDark,
        danger = DangerDark,
        background = InventraNavInk,
        surface = SurfaceDark,
        ink = InkDark,
        muted = MutedDark,
        navBar = NavBarDark,
        isDark = true,
    )

val LocalInventraPalette = staticCompositionLocalOf { LightInventraPalette }

val InventraPurple: Color
    @Composable @ReadOnlyComposable
    get() = LocalInventraPalette.current.purple

val InventraGold: Color
    @Composable @ReadOnlyComposable
    get() = LocalInventraPalette.current.gold

val InventraSuccess: Color
    @Composable @ReadOnlyComposable
    get() = LocalInventraPalette.current.success

val InventraDanger: Color
    @Composable @ReadOnlyComposable
    get() = LocalInventraPalette.current.danger

val InventraBackground: Color
    @Composable @ReadOnlyComposable
    get() = LocalInventraPalette.current.background

val InventraSurface: Color
    @Composable @ReadOnlyComposable
    get() = LocalInventraPalette.current.surface

val InventraInk: Color
    @Composable @ReadOnlyComposable
    get() = LocalInventraPalette.current.ink

val InventraMuted: Color
    @Composable @ReadOnlyComposable
    get() = LocalInventraPalette.current.muted

val InventraNavBar: Color
    @Composable @ReadOnlyComposable
    get() = LocalInventraPalette.current.navBar
