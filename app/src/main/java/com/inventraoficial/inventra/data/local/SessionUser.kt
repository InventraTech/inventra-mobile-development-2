package com.inventraoficial.inventra.data.local

import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class SessionUser(
    val id: String,
    val name: String,
    val email: String,
    val accessType: AccessType,
    val kitchenId: Int? = null,
    val kitchenName: String? = null,
    val photoUrl: String? = null,
)

fun UserResponse.toSessionUser() =
    SessionUser(
        id = id,
        name = name,
        email = email,
        accessType = profile.accessType,
        kitchenId = kitchen?.id,
        kitchenName = kitchen?.name,
        photoUrl = photoUrl,
    )

val SessionUser.mustCreateKitchen: Boolean
    get() = accessType == AccessType.SUPERVISOR && kitchenId == null

val SessionUser.mustJoinKitchen: Boolean
    get() = accessType != AccessType.SUPERVISOR && kitchenId == null
