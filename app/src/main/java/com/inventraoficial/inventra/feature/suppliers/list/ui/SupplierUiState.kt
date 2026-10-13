package com.inventraoficial.inventra.feature.suppliers.list.ui

data class Supplier(
    val id: Int,
    val imageRes: Int,
    val name: String,
    val rating: Int,
    val badgeText: String,
)

data class SupplierUiState(
    val searchQuery: String = "",
    val suppliers: List<Supplier> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
