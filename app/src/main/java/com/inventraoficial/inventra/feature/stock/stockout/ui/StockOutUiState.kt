package com.inventraoficial.inventra.feature.stock.stockout.ui

enum class StockOutReason(
    val label: String,
) {
    Usage("Uso"),
    Loss("Perda"),
    Expired("Vencido"),
}

data class ConsumedBatchLine(
    val batchNumber: String,
    val consumed: String,
    val remaining: String,
)

data class StockOutUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val productName: String = "",
    val details: String? = null,
    val unitSymbol: String = "",
    val available: String = "",
    val nextExpiration: String? = null,
    val hasStock: Boolean = false,
    val quantity: String = "",
    val reason: StockOutReason = StockOutReason.Usage,
    val isConfirmDialogOpen: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val consumedBatches: List<ConsumedBatchLine>? = null,
)
