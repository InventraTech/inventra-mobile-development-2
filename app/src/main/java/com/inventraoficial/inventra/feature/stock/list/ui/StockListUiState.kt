package com.inventraoficial.inventra.feature.stock.list.ui

data class Product(
    val id: Int,
    val photoUrl: String?,
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
    val offlineMessage: String? = null,
    val filters: StockFilters = StockFilters(),
    val draftFilters: StockFilters? = null,
    val categoryOptions: List<String> = emptyList(),
)

data class StockFilters(
    val sort: StockSort = StockSort.Name,
    val categories: Set<String> = emptySet(),
    val statuses: Set<StockStatus> = emptySet(),
) {
    val activeCount: Int
        get() = categories.size + statuses.size + if (sort == StockSort.Name) 0 else 1
}

enum class StockSort(
    val label: String,
) {
    Name("Nome (A–Z)"),
    Expiration("Validade mais próxima"),
    Quantity("Menor quantidade"),
}

enum class StockStatus(
    val label: String,
) {
    ExpiringSoon("Vencendo em breve"),
    LowStock("Abaixo do mínimo"),
    OutOfStock("Sem estoque"),
}

const val ALL_FILTER = "Todos"

data class StockFiltersActions(
    val onSortSelect: (StockSort) -> Unit = {},
    val onCategoryToggle: (String) -> Unit = {},
    val onStatusToggle: (StockStatus) -> Unit = {},
    val onClear: () -> Unit = {},
    val onApply: () -> Unit = {},
)
