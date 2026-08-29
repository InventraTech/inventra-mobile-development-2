package com.inventraoficial.inventra.feature.suppliers.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupplierViewModelTest {
    @Test
    fun `estado inicial mostra todos os fornecedores`() {
        val viewModel = SupplierViewModel()

        assertEquals(5, viewModel.uiState.value.suppliers.size)
    }

    @Test
    fun `onSearchQueryChange filtra por nome`() {
        val viewModel = SupplierViewModel()

        viewModel.onSearchQueryChange("seara")

        val suppliers = viewModel.uiState.value.suppliers
        assertEquals(1, suppliers.size)
        assertEquals("Seara Alimentos", suppliers.first().name)
    }

    @Test
    fun `onSearchQueryChange sem correspondencia retorna lista vazia`() {
        val viewModel = SupplierViewModel()

        viewModel.onSearchQueryChange("fornecedor que nao existe")

        assertTrue(viewModel.uiState.value.suppliers.isEmpty())
    }

    @Test
    fun `onFilterSelect filtra por categoria`() {
        val viewModel = SupplierViewModel()

        viewModel.onFilterSelect("Carnes")

        val suppliers = viewModel.uiState.value.suppliers
        assertEquals(3, suppliers.size)
        assertTrue(suppliers.all { it.category == "Carnes" })
    }

    @Test
    fun `onFilterSelect Todos volta a mostrar todos os fornecedores`() {
        val viewModel = SupplierViewModel()

        viewModel.onFilterSelect("Carnes")
        viewModel.onFilterSelect("Todos")

        assertEquals(5, viewModel.uiState.value.suppliers.size)
    }

    @Test
    fun `filtro de categoria e busca por nome combinam`() {
        val viewModel = SupplierViewModel()

        viewModel.onFilterSelect("Carnes")
        viewModel.onSearchQueryChange("jbs")

        val suppliers = viewModel.uiState.value.suppliers
        assertEquals(1, suppliers.size)
        assertEquals("JBS", suppliers.first().name)
    }
}
