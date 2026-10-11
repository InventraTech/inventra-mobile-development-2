package com.inventraoficial.inventra.data.remote.dto.kitchenaccess

import kotlinx.serialization.Serializable

@Serializable
data class RejectKitchenAccessRequest(
    val reason: String? = null,
)
