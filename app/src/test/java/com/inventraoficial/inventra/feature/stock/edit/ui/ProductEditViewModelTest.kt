package com.inventraoficial.inventra.feature.stock.edit.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ProductEditViewModelTest {
    @Test
    fun `estado inicial carrega os dados do produto`() {
        val viewModel = ProductEditViewModel()

        val uiState = viewModel.uiState.value

        assertEquals("Leite Integral", uiState.productName)
        assertEquals("Laticínios", uiState.category)
        assertEquals("Seara Alimentos", uiState.supplier)
        assertEquals("Câmara fria 02", uiState.storage)
        assertEquals("18 L", uiState.quantity)
    }

    @Test
    fun `onProductNameChange atualiza o nome`() {
        val viewModel = ProductEditViewModel()

        viewModel.onProductNameChange("Leite Desnatado")

        assertEquals("Leite Desnatado", viewModel.uiState.value.productName)
    }

    @Test
    fun `onCategoryChange atualiza a categoria`() {
        val viewModel = ProductEditViewModel()

        viewModel.onCategoryChange("Grãos")

        assertEquals("Grãos", viewModel.uiState.value.category)
    }

    @Test
    fun `onSupplierChange atualiza o fornecedor`() {
        val viewModel = ProductEditViewModel()

        viewModel.onSupplierChange("Piracanjuba")

        assertEquals("Piracanjuba", viewModel.uiState.value.supplier)
    }

    @Test
    fun `onStorageChange atualiza o armazenamento`() {
        val viewModel = ProductEditViewModel()

        viewModel.onStorageChange("Câmara fria 01")

        assertEquals("Câmara fria 01", viewModel.uiState.value.storage)
    }

    @Test
    fun `onQuantityChange atualiza a quantidade`() {
        val viewModel = ProductEditViewModel()

        viewModel.onQuantityChange("24 L")

        assertEquals("24 L", viewModel.uiState.value.quantity)
    }
}
