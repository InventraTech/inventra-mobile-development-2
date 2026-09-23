package com.inventraoficial.inventra.ui.navigation

import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomDestination

fun InventraBottomDestination.toScreen(): Screen =
    when (this) {
        InventraBottomDestination.Home -> Screen.Home
        InventraBottomDestination.Stock -> Screen.StockList
        InventraBottomDestination.Suppliers -> Screen.Suppliers
        // Placeholder: ainda não existe Screen.History
        InventraBottomDestination.History -> Screen.Home
        InventraBottomDestination.Perfil -> Screen.Home
    }

fun Screen.toBottomDestination(): InventraBottomDestination? =
    when (this) {
        Screen.Home -> InventraBottomDestination.Home
        Screen.StockList -> InventraBottomDestination.Stock
        Screen.Suppliers -> InventraBottomDestination.Suppliers
        else -> null
    }
