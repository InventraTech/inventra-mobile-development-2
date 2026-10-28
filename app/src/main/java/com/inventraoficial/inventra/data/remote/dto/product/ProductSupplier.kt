package com.inventraoficial.inventra.data.remote.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class LinkSupplierRequest(
    val supplierId: Int,
    val supplierCode: String? = null,
    val referencePrice: Double? = null,
    val leadTimeDays: Int? = null,
)

@Serializable
data class ProductSupplierResponse(
    val supplier: ProductSupplierSummary,
    val supplierCode: String? = null,
    val referencePrice: Double? = null,
    val leadTimeDays: Int? = null,
)

@Serializable
data class ProductSupplierSummary(
    val id: Int,
    val legalName: String,
)
