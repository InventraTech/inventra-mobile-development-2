package com.inventraoficial.inventra.data.remote.dto.stock

import kotlinx.serialization.Serializable

@Serializable
data class KitchenStockItemResponse(
    val product: ProductSummary,
    val category: StockCategory? = null,
    val unit: StockUnit,
    val totalQuantity: Double,
    val activeBatches: Int,
    val nextExpirationDate: String? = null,
    val minStock: Double? = null,
    val lowStock: Boolean = false,
)

@Serializable
data class StockCategory(
    val id: Int,
    val name: String,
)

@Serializable
data class StockUnit(
    val id: Int,
    val symbol: String,
)
