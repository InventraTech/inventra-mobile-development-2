package com.inventraoficial.inventra.ui.navigation

import androidx.navigation3.runtime.NavKey

sealed interface Screen : NavKey {
    data object Login : Screen

    data object Register : Screen

    data object CreateCozinha : Screen

    data object SelectCozinha : Screen

    data object RequestSent : Screen

    data object Home : Screen

    data object Scan : Screen

    data object Notifications : Screen

    data object Suppliers : Screen

    data object SupplierDetail : Screen

    data object StockList : Screen

    data object StockDetail : Screen

    data object ProductEdit : Screen

    data object StockOut : Screen

    data object Chat : Screen

    data object Profile : Screen

    data object ProfileDetails : Screen

    data object ManageMembers : Screen

    data object AccountPreferences : Screen

    data object CozinhaInfo : Screen
}

/**
 * Telas que ocupam a tela inteira, sem InventraTopBar e sem o padding
 * da barra de navegacao do sistema (formas decorativas encostam nas bordas).
 */
fun Screen.isFullBleed(): Boolean =
    when (this) {
        Screen.Login, Screen.Register, Screen.RequestSent -> true
        else -> false
    }
