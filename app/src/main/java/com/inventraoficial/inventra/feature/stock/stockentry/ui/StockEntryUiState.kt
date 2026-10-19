package com.inventraoficial.inventra.feature.stock.stockentry.ui

import java.time.LocalDate

data class SupplierOption(
    val id: Int,
    val name: String,
)

data class RegisteredBatch(
    val batchNumber: String,
    val quantity: String,
    val expiration: String,
)

data class StockEntryUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val productName: String = "",
    val details: String? = null,
    val unitSymbol: String = "",
    val quantity: String = "",
    val batchNumber: String = "",
    val expirationDate: LocalDate? = null,
    val entryDate: LocalDate? = null,
    val suppliers: List<SupplierOption> = emptyList(),
    val supplierId: Int? = null,
    val invoiceNumber: String = "",
    val unitPrice: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val registeredBatch: RegisteredBatch? = null,
) {
    val supplierName: String?
        get() = suppliers.firstOrNull { it.id == supplierId }?.name
}
