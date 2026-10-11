package com.inventraoficial.inventra.data.remote.dto.stock

import kotlinx.serialization.Serializable

@Serializable
data class LowStockAlertResponse(
    val kitchenId: Int,
    val productId: Int,
    val product: ProductSummary,
    val currentQuantity: Double,
    val minStock: Double,
)
