package com.inventraoficial.inventra.data.remote.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class KitchenParametersRequest(
    val kitchenId: Int,
    val minStock: Double? = null,
    val maxStock: Double? = null,
    val averageDailyConsumption: Double? = null,
)

@Serializable
data class KitchenParametersResponse(
    val kitchen: KitchenParametersKitchen,
    val minStock: Double? = null,
    val maxStock: Double? = null,
    val averageDailyConsumption: Double? = null,
)

@Serializable
data class KitchenParametersKitchen(
    val id: Int,
    val name: String? = null,
)
