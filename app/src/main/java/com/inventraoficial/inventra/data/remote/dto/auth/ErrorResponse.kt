package com.inventraoficial.inventra.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val title: String? = null,
    val detail: String? = null,
    val status: Int? = null,
    val code: String? = null,
    val name: String? = null,
    val email: String? = null,
)
