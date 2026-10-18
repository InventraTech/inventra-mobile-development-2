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

    data class SupplierDetail(
        val supplierId: Int,
    ) : Screen

    data object StockList : Screen

    data class StockDetail(
        val productId: Int,
    ) : Screen

    data object ProductEdit : Screen

    data class StockOut(
        val productId: Int,
    ) : Screen

    data class StockEntry(
        val productId: Int,
    ) : Screen

    // Cadastro de produto pelo supervisor; com o código escaneado, já vem preenchido.
    data class ProductCreate(
        val barcode: String? = null,
    ) : Screen

    data object Chat : Screen

    data object Profile : Screen

    data object ProfileDetails : Screen

    data object ManageMembers : Screen

    data object AccountPreferences : Screen

    data object CozinhaInfo : Screen

    data object RequirementsList : Screen

    data object RequirementsHistory : Screen

    data class RequisitionDetail(
        val requisitionId: Int,
    ) : Screen

    data class RequisitionEditor(
        val requisitionId: Int? = null,
    ) : Screen
}

fun Screen.isFullBleed(): Boolean =
    when (this) {
        Screen.Login, Screen.Register, Screen.RequestSent -> true
        else -> false
    }
