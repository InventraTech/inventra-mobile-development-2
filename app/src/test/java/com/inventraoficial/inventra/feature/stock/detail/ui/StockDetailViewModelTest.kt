package com.inventraoficial.inventra.feature.stock.detail.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StockDetailViewModelTest {
    @Test
    fun `estado inicial carrega os dados do produto`() {
        val viewModel = StockDetailViewModel()

        val uiState = viewModel.uiState.value

        assertEquals("Leite Integral", uiState.productName)
        assertEquals("18 L", uiState.totalValue)
        assertTrue(uiState.totalCaption.isNotBlank())
        assertEquals(3, uiState.batches.size)
        assertEquals("Seara Alimentos", uiState.supplier)
        assertEquals("Câmara fria 02", uiState.storage)
    }

    @Test
    fun `primeiro lote carregado e o mais urgente`() {
        val viewModel = StockDetailViewModel()

        val firstBatch = viewModel.uiState.value.batches
            .first()

        assertTrue(firstBatch.isUrgent)
        assertEquals("AMANHÃ", firstBatch.dateLabel)
    }
}
