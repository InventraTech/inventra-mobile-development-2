package com.inventraoficial.inventra.feature.stock.detail.ui

import com.inventraoficial.inventra.core.designsystem.organisms.InventraBatch

data class StockDetailUiState(
    val productName: String = "",
    val totalLabel: String = "Quantidade total",
    val totalValue: String = "",
    val totalCaption: String = "",
    val batchesTitle: String = "Lotes próximos da validade",
    val batches: List<InventraBatch> = emptyList(),
    val supplier: String = "",
    val storage: String = "",
)
