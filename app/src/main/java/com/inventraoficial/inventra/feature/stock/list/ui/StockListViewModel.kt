package com.inventraoficial.inventra.feature.stock.list.ui

import androidx.lifecycle.ViewModel
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StockListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState.asStateFlow()

    // simula o formato que a API deve devolver no futuro
    private val allProducts: List<Product> = buildFakeProducts()

    init {
        refreshVisibleProducts()
    }

    private fun buildFakeProducts(): List<Product> =
        listOf(
            Product(R.drawable.ic_inventra_logo, "Leite Integral", "18 L", "3 lotes", "Laticínios"),
            Product(R.drawable.ic_inventra_logo, "Arroz Camil", "24 kg", "2 lotes", "Grãos"),
            Product(R.drawable.ic_inventra_logo, "Margarina", "12 un", "1 lote", "Laticínios"),
            Product(R.drawable.ic_inventra_logo, "Farinha de Trigo", "40 kg", "4 lotes", "Grãos"),
            Product(R.drawable.ic_inventra_logo, "Carne Bovina", "80 kg", "5 lotes", "Carnes"),
            Product(R.drawable.ic_inventra_logo, "Frango Congelado", "60 kg", "3 lotes", "Carnes"),
        )

    private fun refreshVisibleProducts() {
        val query = _uiState.value.searchQuery
        val filter = _uiState.value.selectedFilter

        val visibleProducts =
            allProducts.filter { product ->
                val matchesFilter = filter == "Todos" || product.category == filter
                val matchesQuery = product.title.contains(query, ignoreCase = true)
                matchesFilter && matchesQuery
            }

        _uiState.value = _uiState.value.copy(products = visibleProducts)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        refreshVisibleProducts()
    }

    fun onFilterSelect(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        refreshVisibleProducts()
    }

    fun onFilterIconClick() {
        // sem comportamento definido ainda
    }

    fun onProductClick(product: Product) {
        // sem navegação configurada ainda
    }

    fun onQrScanClick() {
        // sem navegação configurada ainda
    }

    fun onBottomNavSelect(destination: InventraBottomDestination) {
        // sem navegação configurada ainda
    }
}
