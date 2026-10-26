package com.inventraoficial.inventra.data.remote.dto.stock

import com.inventraoficial.inventra.data.remote.dto.auth.KitchenSummary
import kotlinx.serialization.Serializable

@Serializable
data class StockBatchResponse(
    val id: Int,
    val product: ProductSummary,
    val kitchen: KitchenSummary,
    val supplier: SupplierSummary? = null,
    val batchNumber: String,
    val invoiceNumber: String? = null,
    val initialQuantity: Double,
    val currentQuantity: Double,
    val entryDate: String,
    val expirationDate: String,
    val unitPrice: Double? = null,
    val status: StockBatchStatus,
)

@Serializable
data class ProductSummary(
    val id: Int,
    val name: String,
    val photoUrl: String? = null,
)

@Serializable
data class SupplierSummary(
    val id: Int,
    val legalName: String,
)

@Serializable
enum class StockBatchStatus {
    ACTIVE,
    WRITTEN_OFF,
    EXPIRED,
    CANCELLED,
}
