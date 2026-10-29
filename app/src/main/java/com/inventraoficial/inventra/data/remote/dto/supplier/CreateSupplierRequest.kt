package com.inventraoficial.inventra.data.remote.dto.supplier

import kotlinx.serialization.Serializable

@Serializable
data class CreateSupplierRequest(
    val legalName: String,
    val cnpj: String,
    val email: String? = null,
    val whatsapp: String? = null,
    val rating: Int? = null,
)
