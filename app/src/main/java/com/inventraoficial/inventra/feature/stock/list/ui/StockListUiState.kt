package com.inventraoficial.inventra.feature.stock.list.ui

data class Product(
    val imageRes: Int,
    val title: String,
    val quantity: String,
    val batchInfo: String,
    val category: String,
)

data class StockListUiState(
    val searchQuery: String = "",
    val filterOptions: List<String> = listOf("Todos", "Carnes", "Laticínios", "Grãos"),
    val selectedFilter: String = "Todos",
    val products: List<Product> = emptyList(),
)
