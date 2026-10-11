package com.inventraoficial.inventra.data.remote.dto.kitchenaccess

import com.inventraoficial.inventra.data.remote.dto.auth.KitchenSummary
import kotlinx.serialization.Serializable

@Serializable
data class KitchenAccessRequestResponse(
    val id: String,
    val status: KitchenAccessRequestStatus,
    val kitchen: KitchenSummary,
    val user: AccessRequestUser,
    val createdAt: String,
    val expiresAt: String? = null,
    val decidedAt: String? = null,
    val reason: String? = null,
)

@Serializable
data class AccessRequestUser(
    val id: String,
    val name: String,
    val email: String,
)

@Serializable
enum class KitchenAccessRequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
}
