package com.inventraoficial.inventra.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class GoogleLoginRequest(
    val idToken: String,
    val accessType: AccessType? = null,
)
