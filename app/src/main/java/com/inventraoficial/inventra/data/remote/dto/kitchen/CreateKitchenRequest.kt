package com.inventraoficial.inventra.data.remote.dto.kitchen

import kotlinx.serialization.Serializable

@Serializable
data class CreateKitchenRequest(
    val name: String,
    val address: String? = null,
)
