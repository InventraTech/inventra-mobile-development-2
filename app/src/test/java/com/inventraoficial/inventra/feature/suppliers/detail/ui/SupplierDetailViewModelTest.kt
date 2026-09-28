package com.inventraoficial.inventra.feature.suppliers.detail.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupplierDetailViewModelTest {
    @Test
    fun `estado inicial carrega os dados do fornecedor`() {
        val viewModel = SupplierDetailViewModel()

        val uiState = viewModel.uiState.value

        assertEquals("Seara Alimentos", uiState.name)
        assertEquals(5, uiState.rating)
        assertEquals("VPQ 50", uiState.badgeText)
        assertTrue(uiState.phone.isNotBlank())
        assertTrue(uiState.email.isNotBlank())
        assertTrue(uiState.address.isNotBlank())
    }

    @Test
    fun `carrega os produtos fornecidos`() {
        val viewModel = SupplierDetailViewModel()

        val products = viewModel.uiState.value.products

        assertEquals(2, products.size)
        assertEquals("Arroz Camil", products.first().name)
    }

    @Test
    fun `carrega o historico de entregas`() {
        val viewModel = SupplierDetailViewModel()

        val deliveryHistory = viewModel.uiState.value.deliveryHistory

        assertEquals(2, deliveryHistory.size)
        assertEquals("Arroz Camil", deliveryHistory.first().productName)
    }
}
