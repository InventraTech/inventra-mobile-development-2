package com.inventraoficial.inventra.data.remote.dto.kitchenaccess

import kotlinx.serialization.Serializable

@Serializable
data class CreateKitchenAccessRequest(
    val kitchenId: Int,
)
