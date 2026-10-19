package com.inventraoficial.inventra.data.remote.dto.stock

import kotlinx.serialization.Serializable

// Datas no formato ISO (yyyy-MM-dd), como o backend espera.
@Serializable
data class RegisterStockEntryRequest(
    val productId: Int,
    val kitchenId: Int,
    val batchNumber: String,
    val initialQuantity: Double,
    val entryDate: String,
    val expirationDate: String?,
    val supplierId: Int? = null,
    val invoiceNumber: String? = null,
    val unitPrice: Double? = null,
)
