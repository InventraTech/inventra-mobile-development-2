package com.inventraoficial.inventra.data.remote.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class PageProductResponse(
    val content: List<ProductResponse> = emptyList(),
)
