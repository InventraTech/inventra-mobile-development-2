package com.inventraoficial.inventra.feature.suppliers.ui

data class Supplier(
    val imageRes: Int,
    val name: String,
    val rating: Int,
    val badgeText: String,
    val category: String,
)

data class SupplierUiState(
    val searchQuery: String = "",
    val filterOptions: List<String> = listOf("Todos", "Carnes", "Laticínios", "Grãos"),
    val selectedFilter: String = "Todos",
    val suppliers: List<Supplier> = emptyList(),
)
