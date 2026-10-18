package com.inventraoficial.inventra.data.remote.dto.stock

import kotlinx.serialization.Serializable

@Serializable
data class ConsumeProductRequest(
    val kitchenId: Int,
    val productId: Int,
    val quantity: Double,
    val reason: StockMovementReason,
)

@Serializable
enum class StockMovementReason {
    USAGE,
    LOSS,
    EXPIRED,
}

@Serializable
data class BatchConsumptionResponse(
    val batch: ConsumedBatch,
    val consumedQuantity: Double,
    val remainingQuantity: Double,
    val status: StockBatchStatus,
)

@Serializable
data class ConsumedBatch(
    val id: Int,
    val batchNumber: String,
    val expirationDate: String? = null,
)
