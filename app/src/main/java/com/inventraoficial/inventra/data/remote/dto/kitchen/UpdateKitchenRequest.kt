package com.inventraoficial.inventra.data.remote.dto.kitchen

import kotlinx.serialization.Serializable

// Campo nulo fica como está no backend. Endereço novo sem coordenadas faz o backend buscar a localização sozinho.
@Serializable
data class UpdateKitchenRequest(
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val acceptingRequests: Boolean? = null,
)
