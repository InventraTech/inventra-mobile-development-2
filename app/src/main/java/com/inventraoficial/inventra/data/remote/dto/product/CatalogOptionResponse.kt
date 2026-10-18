package com.inventraoficial.inventra.data.remote.dto.product

import kotlinx.serialization.Serializable

// Unidades e categorias que o cadastro de produto oferece para escolha.
@Serializable
data class UnitResponse(
    val id: Int,
    val symbol: String,
    val description: String? = null,
)

@Serializable
data class CategoryResponse(
    val id: Int,
    val name: String,
    val description: String? = null,
)
