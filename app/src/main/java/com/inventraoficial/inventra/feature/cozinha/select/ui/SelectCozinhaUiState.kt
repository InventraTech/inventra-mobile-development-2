package com.inventraoficial.inventra.feature.cozinha.select.ui

import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRequestStatus
import com.inventraoficial.inventra.data.local.GeoPoint

enum class CozinhaViewMode { List, Map }

data class CozinhaListItem(
    val id: Int,
    val name: String,
    val location: String?,
    val distance: String?,
    val status: InventraCozinhaRequestStatus,
    val latitude: Double? = null,
    val longitude: Double? = null,
) {
    val subtitle: String
        get() = listOfNotNull(location, distance).joinToString(" · ").ifEmpty { "Localização não informada" }
}

data class SelectCozinhaUiState(
    val searchQuery: String = "",
    val viewMode: CozinhaViewMode = CozinhaViewMode.List,
    val cozinhas: List<CozinhaListItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedCozinha: CozinhaListItem? = null,
    val isRequesting: Boolean = false,
    val requestErrorMessage: String? = null,
    val isRequestSent: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isNearMe: Boolean = false,
    val isLocating: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val userLocation: GeoPoint? = null,
    val locationMessage: String? = null,
    // Nome da cozinha do pedido pendente: com ele, o mapa só mostra as cozinhas e não deixa pedir em outra.
    val pendingCozinhaName: String? = null,
)
