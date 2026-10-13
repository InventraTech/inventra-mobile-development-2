package com.inventraoficial.inventra.feature.stock.list.ui

data class Product(
    val id: Int,
    val imageRes: Int,
    val title: String,
    val quantity: String,
    val batchInfo: String,
    val category: String,
)

data class StockListUiState(
    val searchQuery: String = "",
    val filterOptions: List<String> = listOf(ALL_FILTER),
    val selectedFilter: String = ALL_FILTER,
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

const val ALL_FILTER = "Todos"
