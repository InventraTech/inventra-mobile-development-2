package com.inventraoficial.inventra.data.local

class FakeLocationProvider(
    var result: Result<GeoPoint> = Result.success(paulista),
) : LocationProvider {
    var calls = 0
        private set

    override suspend fun currentLocation(): Result<GeoPoint> {
        calls++
        return result
    }

    companion object {
        val paulista = GeoPoint(latitude = -23.5614, longitude = -46.6559)
    }
}
