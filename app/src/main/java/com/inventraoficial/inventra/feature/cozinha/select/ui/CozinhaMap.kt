package com.inventraoficial.inventra.feature.cozinha.select.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRequestStatus
import com.inventraoficial.inventra.data.local.GeoPoint
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraSurface
import com.inventraoficial.inventra.ui.theme.Montserrat

private const val DEFAULT_ZOOM = 10f
private const val SINGLE_PIN_ZOOM = 14f
private const val SAO_PAULO_LAT = -23.55052
private const val SAO_PAULO_LNG = -46.633308
private val DEFAULT_CENTER = LatLng(SAO_PAULO_LAT, SAO_PAULO_LNG)
private val BOUNDS_PADDING = 64.dp

@Composable
fun CozinhaMap(
    cozinhas: List<CozinhaListItem>,
    userLocation: GeoPoint?,
    showMyLocation: Boolean,
    onCozinhaClick: (CozinhaListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pinned = remember(cozinhas) { cozinhas.mapNotNull { cozinha -> cozinha.position()?.let { cozinha to it } } }
    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(DEFAULT_CENTER, DEFAULT_ZOOM)
        }
    var isMapLoaded by remember { mutableStateOf(false) }
    val boundsPadding = with(LocalDensity.current) { BOUNDS_PADDING.roundToPx() }

    LaunchedEffect(isMapLoaded, pinned, userLocation) {
        if (!isMapLoaded) return@LaunchedEffect
        val points = pinned.map { it.second } + listOfNotNull(userLocation?.let { LatLng(it.latitude, it.longitude) })
        when (points.size) {
            0 -> Unit
            1 -> cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(points.first(), SINGLE_PIN_ZOOM))
            else -> {
                val bounds = LatLngBounds.builder().apply { points.forEach(::include) }.build()
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, boundsPadding))
            }
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = showMyLocation),
            uiSettings =
                MapUiSettings(
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = showMyLocation,
                    zoomControlsEnabled = false,
                ),
            onMapLoaded = { isMapLoaded = true },
        ) {
            pinned.forEach { (cozinha, position) ->
                Marker(
                    state = rememberUpdatedMarkerState(position = position),
                    title = cozinha.name,
                    icon = BitmapDescriptorFactory.defaultMarker(cozinha.status.markerHue()),
                    onClick = {
                        onCozinhaClick(cozinha)
                        true
                    },
                )
            }
        }

        val withoutPin = cozinhas.size - pinned.size
        if (withoutPin > 0) {
            Text(
                text = withoutPinText(withoutPin, noneOnMap = pinned.isEmpty()),
                fontFamily = Montserrat,
                fontSize = 12.sp,
                color = InventraInk,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(InventraSurface.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
            )
        }
    }
}

private fun CozinhaListItem.position(): LatLng? {
    if (latitude == null || longitude == null) return null
    return LatLng(latitude, longitude)
}

private fun InventraCozinhaRequestStatus.markerHue(): Float =
    when (this) {
        InventraCozinhaRequestStatus.Disponivel -> BitmapDescriptorFactory.HUE_VIOLET
        InventraCozinhaRequestStatus.Pendente -> BitmapDescriptorFactory.HUE_YELLOW
        InventraCozinhaRequestStatus.Recusada -> BitmapDescriptorFactory.HUE_RED
    }

private fun withoutPinText(
    count: Int,
    noneOnMap: Boolean,
): String =
    when {
        noneOnMap -> "Nenhuma cozinha tem localização no mapa ainda. Use a lista."
        count == 1 -> "1 cozinha sem localização aparece só na lista."
        else -> "$count cozinhas sem localização aparecem só na lista."
    }
