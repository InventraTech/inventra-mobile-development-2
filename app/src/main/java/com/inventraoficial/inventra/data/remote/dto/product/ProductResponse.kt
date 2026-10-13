package com.inventraoficial.inventra.data.remote.dto.product

import com.inventraoficial.inventra.data.remote.dto.stock.StockCategory
import com.inventraoficial.inventra.data.remote.dto.stock.StockUnit
import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    val id: Int,
    val name: String,
    val brand: String? = null,
    val category: StockCategory? = null,
    val unit: StockUnit,
    val barcode: String? = null,
    val photoUrl: String? = null,
    val active: Boolean = true,
)
