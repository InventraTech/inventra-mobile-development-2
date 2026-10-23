package com.inventraoficial.inventra.ui.navigation

import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomDestination
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType

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

fun bottomDestinationsFor(accessType: AccessType?): List<InventraBottomDestination> =
    when (accessType) {
        AccessType.ESTOQUISTA -> InventraBottomDestination.entries - InventraBottomDestination.History
        AccessType.COMPRADOR ->
            listOf(
                InventraBottomDestination.Suppliers,
                InventraBottomDestination.History,
                InventraBottomDestination.Perfil,
            )
        else -> InventraBottomDestination.entries
    }

fun homeScreenFor(accessType: AccessType?): Screen =
    when (accessType) {
        AccessType.COMPRADOR -> Screen.RequirementsList
        else -> Screen.Home
    }
