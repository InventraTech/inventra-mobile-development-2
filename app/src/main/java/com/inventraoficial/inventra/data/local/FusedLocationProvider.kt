package com.inventraoficial.inventra.data.local

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class FusedLocationProvider(
    private val context: Context,
) : LocationProvider {
    // A permissão é pedida pela tela antes de chamar; se faltar, o SecurityException vira a mensagem de permissão.
    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(): Result<GeoPoint> =
        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val location =
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
                    ?: client.lastLocation.await()
            if (location == null) {
                Result.failure(Exception(UNAVAILABLE_ERROR))
            } else {
                Result.success(GeoPoint(location.latitude, location.longitude))
            }
        } catch (e: SecurityException) {
            Result.failure(Exception(PERMISSION_ERROR, e))
        } catch (e: ApiException) {
            Result.failure(Exception(UNAVAILABLE_ERROR, e))
        }

    private companion object {
        const val PERMISSION_ERROR = "Permita a localização para ver as cozinhas perto de você."
        const val UNAVAILABLE_ERROR = "Não foi possível obter sua localização. Verifique se a localização está ligada."
    }
}
