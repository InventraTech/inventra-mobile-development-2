package com.inventraoficial.inventra.data.remote.dto.kitchen

import kotlinx.serialization.Serializable

@Serializable
data class KitchenResponse(
    val id: Int,
    val name: String,
    val code: String,
    val address: String? = null,
    val active: Boolean,
    val createdAt: String,
)
