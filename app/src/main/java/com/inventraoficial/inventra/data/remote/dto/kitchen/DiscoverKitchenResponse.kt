package com.inventraoficial.inventra.data.remote.dto.kitchen

import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestStatus
import kotlinx.serialization.Serializable

@Serializable
data class DiscoverKitchenResponse(
    val id: Int,
    val name: String,
    val city: String? = null,
    val state: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distanceKm: Double? = null,
    val myRequestStatus: KitchenAccessRequestStatus? = null,
)

@Serializable
data class DiscoverKitchenPage(
    val content: List<DiscoverKitchenResponse>,
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
)
