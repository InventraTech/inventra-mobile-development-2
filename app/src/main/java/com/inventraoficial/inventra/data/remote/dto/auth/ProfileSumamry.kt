package com.inventraoficial.inventra.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ProfileSumamry(
    val id: Int,
    val accessType: AccessType,
)
