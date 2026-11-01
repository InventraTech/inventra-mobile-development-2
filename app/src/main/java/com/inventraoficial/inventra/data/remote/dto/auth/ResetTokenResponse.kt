package com.inventraoficial.inventra.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ResetTokenResponse(
    val resetToken: String,
    val expiresInSeconds: Long,
)
