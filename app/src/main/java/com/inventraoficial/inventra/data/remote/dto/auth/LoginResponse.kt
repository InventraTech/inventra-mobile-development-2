package com.inventraoficial.inventra.data.remote.dto.auth

import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: UserResponse,
)
