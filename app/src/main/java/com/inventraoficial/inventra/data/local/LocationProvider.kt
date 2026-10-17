package com.inventraoficial.inventra.data.local

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

interface LocationProvider {
    suspend fun currentLocation(): Result<GeoPoint>
}
