package com.inventraoficial.inventra.data.remote.dto.prelist

import com.inventraoficial.inventra.data.remote.dto.stock.SupplierSummary
import kotlinx.serialization.Serializable

@Serializable
data class PreListSummaryResponse(
    val id: Int,
    val listDate: String,
)

@Serializable
data class PreListResponse(
    val id: Int,
    val listDate: String,
    val items: List<PreListItemResponse> = emptyList(),
)

@Serializable
data class PreListItemResponse(
    val id: Int,
    val product: PreListProduct,
    val supplier: SupplierSummary? = null,
    val quantity: Double,
)

@Serializable
data class PreListProduct(
    val id: Int,
    val name: String,
    val photoUrl: String? = null,
)
