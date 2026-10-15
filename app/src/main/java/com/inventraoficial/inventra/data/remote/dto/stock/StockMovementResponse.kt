package com.inventraoficial.inventra.data.remote.dto.stock

import kotlinx.serialization.Serializable

@Serializable
data class StockMovementResponse(
    val id: Int,
    val type: StockMovementType,
    val reason: String? = null,
    val quantity: Double,
    val balanceAfter: Double,
    val batch: MovementBatch,
    val product: ProductSummary,
    val user: MovementUser? = null,
    val requisitionId: Int? = null,
    val createdAt: String,
)

@Serializable
enum class StockMovementType {
    ENTRY,
    WRITE_OFF,
    ADJUSTMENT,
}

@Serializable
data class MovementBatch(
    val id: Int,
    val batchNumber: String,
)

@Serializable
data class MovementUser(
    val id: String,
    val name: String,
)
