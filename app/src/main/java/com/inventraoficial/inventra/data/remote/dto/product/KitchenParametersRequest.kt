package com.inventraoficial.inventra.data.remote.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class KitchenParametersRequest(
    val kitchenId: Int,
    val minStock: Double? = null,
)
