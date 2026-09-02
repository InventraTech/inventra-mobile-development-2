package com.inventraoficial.inventra.feature.stock.stockout.ui

data class StockOutUiState(
    val productName: String = "",
    val batchCode: String = "",
    val quantity: Int = 0,
    val reason: String = "",
)
