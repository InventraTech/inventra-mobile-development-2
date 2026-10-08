package com.inventraoficial.inventra.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
enum class AccessType {
    SUPERVISOR,
    ESTOQUISTA,
    COMPRADOR,
}
