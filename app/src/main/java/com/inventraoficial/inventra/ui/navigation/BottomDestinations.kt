package com.inventraoficial.inventra.ui.navigation

import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomDestination

fun InventraBottomDestination.toScreen(): Screen =
    when (this) {
        InventraBottomDestination.Home -> Screen.Home
        InventraBottomDestination.Stock -> Screen.StockList
        InventraBottomDestination.Suppliers -> Screen.Suppliers
        InventraBottomDestination.History -> Screen.RequirementsList
        InventraBottomDestination.Perfil -> Screen.Profile
    }

fun Screen.toBottomDestination(): InventraBottomDestination? =
    when (this) {
        Screen.Home -> InventraBottomDestination.Home
        Screen.StockList -> InventraBottomDestination.Stock
        Screen.Suppliers -> InventraBottomDestination.Suppliers
        Screen.RequirementsList -> InventraBottomDestination.History
        Screen.RequirementsHistory -> InventraBottomDestination.History
        Screen.Profile -> InventraBottomDestination.Perfil
        else -> null
    }
