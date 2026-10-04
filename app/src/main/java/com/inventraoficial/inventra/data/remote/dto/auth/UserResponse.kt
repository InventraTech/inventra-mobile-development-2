package com.inventraoficial.inventra.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val kitchen: KitchenSummary? = null,
    val profile: ProfileSummary,
    val active: Boolean,
    val lastLogin: String? = null,
    val createdAt: String,
)
