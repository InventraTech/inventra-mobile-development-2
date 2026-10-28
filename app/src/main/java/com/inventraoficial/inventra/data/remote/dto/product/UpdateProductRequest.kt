package com.inventraoficial.inventra.data.remote.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProductRequest(
    val name: String,
    val brand: String? = null,
    val categoryId: Int? = null,
    val unitId: Int,
    val barcode: String? = null,
)
