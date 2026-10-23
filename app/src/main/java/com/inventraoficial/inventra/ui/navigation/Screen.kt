package com.inventraoficial.inventra.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Login : Screen

    @Serializable
    data object Register : Screen

    @Serializable
    data class ForgotPassword(
        val email: String = "",
    ) : Screen

    @Serializable
    data class ResetCode(
        val email: String,
    ) : Screen

    @Serializable
    data class NewPassword(
        val email: String,
        val resetToken: String,
    ) : Screen

    @Serializable
    data object CreateCozinha : Screen

    @Serializable
    data object SelectCozinha : Screen

    @Serializable
    data object RequestSent : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Scan : Screen

    @Serializable
    data object Notifications : Screen

    @Serializable
    data object Suppliers : Screen

    @Serializable
    data class SupplierDetail(
        val supplierId: Int,
    ) : Screen

    @Serializable
    data object StockList : Screen

    @Serializable
    data class StockDetail(
        val productId: Int,
    ) : Screen

    @Serializable
    data class ProductEdit(
        val productId: Int,
    ) : Screen

    @Serializable
    data class StockOut(
        val productId: Int,
    ) : Screen

    @Serializable
    data class StockEntry(
        val productId: Int,
    ) : Screen

    // Cadastro de produto pelo supervisor; com o código escaneado, já vem preenchido.
    @Serializable
    data class ProductCreate(
        val barcode: String? = null,
    ) : Screen

    @Serializable
    data object Chat : Screen

    @Serializable
    data object Profile : Screen

    @Serializable
    data object ProfileDetails : Screen

    @Serializable
    data object ManageMembers : Screen

    @Serializable
    data object AccountPreferences : Screen

    @Serializable
    data object CozinhaInfo : Screen

    @Serializable
    data object RequirementsList : Screen

    @Serializable
    data object RequirementsHistory : Screen

    @Serializable
    data class RequisitionDetail(
        val requisitionId: Int,
    ) : Screen

    @Serializable
    data class RequisitionEditor(
        val requisitionId: Int? = null,
    ) : Screen

    @Serializable
    data class PreListDetail(
        val preListId: Int,
    ) : Screen
}

fun Screen.isFullBleed(): Boolean =
    when (this) {
        Screen.Login, Screen.Register, Screen.RequestSent -> true
        is Screen.ForgotPassword, is Screen.ResetCode, is Screen.NewPassword -> true
        else -> false
    }
