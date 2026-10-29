package com.inventraoficial.inventra.feature.stock.detail.ui

import com.inventraoficial.inventra.core.designsystem.organisms.InventraBatch

data class StockDetailUiState(
    val productPhotoUrl: String? = null,
    val productName: String = "",
    val productDetails: String? = null,
    val totalValue: String = "",
    val totalCaption: String = "",
    val batches: List<InventraBatch> = emptyList(),
    val seeAllBatchesText: String? = null,
    val supplier: String = "",
    val supplierId: Int? = null,
    val history: HistoryState? = null,
    val canEdit: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class HistoryState(
    val type: HistoryType,
    val entries: List<HistoryEntry> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

enum class HistoryType(
    val title: String,
    val emptyText: String,
) {
    Entries("Entradas", "Nenhuma entrada registrada."),
    Exits("Saídas", "Nenhuma saída registrada."),
}

data class HistoryEntry(
    val title: String,
    val detail: String,
    val quantityLabel: String,
)

data class StockDetailActions(
    val onSeeAllBatchesClick: () -> Unit = {},
    val onEntriesClick: () -> Unit = {},
    val onExitsClick: () -> Unit = {},
    val onStockEntryClick: () -> Unit = {},
    val onStockOutClick: () -> Unit = {},
    val onEditClick: () -> Unit = {},
    val onSupplierClick: (Int) -> Unit = {},
    val onRetryClick: () -> Unit = {},
    val onBackClick: () -> Unit = {},
)
