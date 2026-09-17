package com.inventraoficial.inventra.feature.stock.stockout.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class StockOutViewModelTest {
    @Test
    fun `estado inicial carrega produto e lote`() {
        val viewModel = StockOutViewModel()

        val uiState = viewModel.uiState.value

        assertEquals("Arroz Camil", uiState.productName)
        assertEquals("000067", uiState.batchCode)
        assertEquals(0, uiState.quantity)
        assertEquals("", uiState.reason)
    }

    @Test
    fun `onQuantityChange atualiza a quantidade`() {
        val viewModel = StockOutViewModel()

        viewModel.onQuantityChange(5)

        assertEquals(5, viewModel.uiState.value.quantity)
    }

    @Test
    fun `onReasonChange atualiza o motivo`() {
        val viewModel = StockOutViewModel()

        viewModel.onReasonChange("Produto vencido")

        assertEquals("Produto vencido", viewModel.uiState.value.reason)
    }
}
