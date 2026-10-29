package com.inventraoficial.inventra.data.remote.dto.product

import kotlinx.serialization.Serializable

// Só o supervisor cadastra produtos. A foto não vai aqui: o backend só aceita pelo upload.
@Serializable
data class CreateProductRequest(
    val name: String,
    val brand: String? = null,
    val categoryId: Int? = null,
    val unitId: Int,
    val barcode: String? = null,
    val supplierId: Int,
    val referencePrice: Double? = null,
)
