package com.inventraoficial.inventra.feature.stock.list.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StockListViewModelTest {
    @Test
    fun `estado inicial mostra todos os produtos`() {
        val viewModel = StockListViewModel()

        assertEquals(6, viewModel.uiState.value.products.size)
    }

    @Test
    fun `onSearchQueryChange filtra por nome`() {
        val viewModel = StockListViewModel()

        viewModel.onSearchQueryChange("leite")

        val products = viewModel.uiState.value.products
        assertEquals(1, products.size)
        assertEquals("Leite Integral", products.first().title)
    }

    @Test
    fun `onSearchQueryChange sem correspondencia retorna lista vazia`() {
        val viewModel = StockListViewModel()

        viewModel.onSearchQueryChange("produto que nao existe")

        assertTrue(viewModel.uiState.value.products.isEmpty())
    }

    @Test
    fun `onFilterSelect filtra por categoria`() {
        val viewModel = StockListViewModel()

        viewModel.onFilterSelect("Carnes")

        val products = viewModel.uiState.value.products
        assertEquals(2, products.size)
        assertTrue(products.all { it.category == "Carnes" })
    }

    @Test
    fun `onFilterSelect Todos volta a mostrar todos os produtos`() {
        val viewModel = StockListViewModel()

        viewModel.onFilterSelect("Carnes")
        viewModel.onFilterSelect("Todos")

        assertEquals(6, viewModel.uiState.value.products.size)
    }

    @Test
    fun `filtro de categoria e busca por nome combinam`() {
        val viewModel = StockListViewModel()

        viewModel.onFilterSelect("Grãos")
        viewModel.onSearchQueryChange("arroz")

        val products = viewModel.uiState.value.products
        assertEquals(1, products.size)
        assertEquals("Arroz Camil", products.first().title)
    }
}
