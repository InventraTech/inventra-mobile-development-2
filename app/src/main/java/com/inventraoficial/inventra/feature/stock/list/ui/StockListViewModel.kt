package com.inventraoficial.inventra.feature.stock.list.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.data.remote.dto.stock.KitchenStockItemResponse
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class StockListViewModel(
    private val authRepository: AuthRepository,
    private val stockRepository: StockRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState.asStateFlow()

    private var allProducts: List<Product> = emptyList()

    fun onScreenVisible() {
        load()
    }

    fun onRetryClick() {
        load()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshVisibleProducts()
    }

    fun onFilterSelect(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        refreshVisibleProducts()
    }

    fun onFilterIconClick() = Unit

    private fun load() {
        _uiState.update { it.copy(isLoading = allProducts.isEmpty(), errorMessage = null) }
        viewModelScope.launch {
            val kitchenId = authRepository.currentUser.first()?.kitchenId
            if (kitchenId == null) {
                showProducts(emptyList())
                return@launch
            }
            stockRepository
                .kitchenStock(kitchenId)
                .onSuccess { items -> showProducts(items.map { it.toProduct() }) }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun showProducts(products: List<Product>) {
        allProducts = products
        val options = listOf(ALL_FILTER) + products.map { it.category }.distinct().sorted()
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                filterOptions = options,
                selectedFilter = state.selectedFilter.takeIf { it in options } ?: ALL_FILTER,
            )
        }
        refreshVisibleProducts()
    }

    private fun refreshVisibleProducts() {
        val query = _uiState.value.searchQuery.trim()
        val filter = _uiState.value.selectedFilter
        val visibleProducts =
            allProducts.filter { product ->
                val matchesFilter = filter == ALL_FILTER || product.category == filter
                val matchesQuery = product.title.contains(query, ignoreCase = true)
                matchesFilter && matchesQuery
            }
        _uiState.update { it.copy(products = visibleProducts) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    StockListViewModel(
                        authRepository = application.container.authRepository,
                        stockRepository = application.container.stockRepository,
                    )
                }
            }
    }
}

private const val NO_CATEGORY = "Sem categoria"

private val quantityFormat = DecimalFormat("#,##0.###", DecimalFormatSymbols(Locale.forLanguageTag("pt-BR")))

private fun KitchenStockItemResponse.toProduct(): Product {
    val batches =
        when (activeBatches) {
            0 -> "Sem lotes"
            1 -> "1 lote"
            else -> "$activeBatches lotes"
        }
    return Product(
        id = product.id,
        imageRes = R.drawable.ic_inventra_logo,
        title = product.name,
        quantity = "${quantityFormat.format(totalQuantity)} ${unit.symbol}",
        batchInfo = if (lowStock) "$batches · abaixo do mínimo" else batches,
        category = category?.name ?: NO_CATEGORY,
    )
}
