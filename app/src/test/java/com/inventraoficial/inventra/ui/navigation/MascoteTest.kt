package com.inventraoficial.inventra.ui.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MascoteTest {
    @Test
    fun `mascote aparece nas telas de navegacao`() {
        listOf(
            Screen.Home,
            Screen.StockList,
            Screen.Suppliers,
            Screen.RequirementsList,
            Screen.RequirementsHistory,
            Screen.Profile,
            Screen.ProfileDetails,
            Screen.Notifications,
            Screen.StockDetail(1),
            Screen.SupplierDetail(1),
            Screen.RequisitionDetail(1),
        ).forEach { assertTrue(it.toString(), it.mostraMascote()) }
    }

    @Test
    fun `mascote fica de fora de entrada, chat, leitor e formularios`() {
        listOf(
            Screen.Login,
            Screen.Register,
            Screen.CreateCozinha,
            Screen.SelectCozinha,
            Screen.RequestSent,
            Screen.Chat,
            Screen.Scan,
            Screen.ProductCreate(),
            Screen.ProductEdit(1),
            Screen.StockOut(1),
            Screen.StockEntry(1),
            Screen.RequisitionEditor(),
        ).forEach { assertFalse(it.toString(), it.mostraMascote()) }
    }
}
