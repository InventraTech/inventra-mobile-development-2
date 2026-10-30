package com.inventraoficial.inventra.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class VerifyResetCodeRequest(
    val email: String,
    val code: String,
)
