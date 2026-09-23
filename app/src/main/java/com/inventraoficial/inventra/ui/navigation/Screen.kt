package com.inventraoficial.inventra.ui.navigation

import androidx.navigation3.runtime.NavKey

sealed interface Screen : NavKey {
    data object Login : Screen

    data object Home : Screen

    data object Scan : Screen

    data object Notifications : Screen

    data object Suppliers : Screen

    data object StockList : Screen

    data object StockDetail : Screen

    data object ProductEdit : Screen

    data object StockOut : Screen

    data object Chat : Screen
}
