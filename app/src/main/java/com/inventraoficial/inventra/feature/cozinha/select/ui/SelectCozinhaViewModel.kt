package com.inventraoficial.inventra.feature.cozinha.select.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRequestStatus
import com.inventraoficial.inventra.data.local.LocationProvider
import com.inventraoficial.inventra.data.remote.dto.kitchen.DiscoverKitchenResponse
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestStatus
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.KitchenAccessRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@Suppress("TooManyFunctions")
class SelectCozinhaViewModel(
    private val kitchenAccessRepository: KitchenAccessRepository,
    private val authRepository: AuthRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SelectCozinhaUiState())
    val uiState: StateFlow<SelectCozinhaUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        load()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_MS)
                fetch()
            }
    }

    fun onRetryClick() = load()

    fun onFilterIconClick() = Unit

    fun onViewModeSelect(viewMode: CozinhaViewMode) {
        _uiState.update { it.copy(viewMode = viewMode) }
    }

    fun onCozinhaClick(cozinha: CozinhaListItem) {
        _uiState.update { it.copy(selectedCozinha = cozinha, requestErrorMessage = null) }
    }

    fun onSheetDismiss() {
        if (_uiState.value.isRequesting) return
        _uiState.update { it.copy(selectedCozinha = null, requestErrorMessage = null) }
    }

    fun onRequestAccessClick() {
        val state = _uiState.value
        val target = state.selectedCozinha ?: return
        if (state.isRequesting || target.status == InventraCozinhaRequestStatus.Pendente) return

        _uiState.update { it.copy(isRequesting = true, requestErrorMessage = null) }
        viewModelScope.launch {
            kitchenAccessRepository
                .requestAccess(target.id)
                .onSuccess { _uiState.update { it.copy(isRequesting = false, isRequestSent = true) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isRequesting = false, requestErrorMessage = error.message) }
                }
        }
    }

    fun onLocationPermissionChecked(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
        if (granted) {
            locate()
        } else {
            _uiState.update { it.copy(locationMessage = PERMISSION_DENIED) }
        }
    }

    fun onNearMeClick() {
        val state = _uiState.value
        if (state.isLocating) return
        if (state.isNearMe) {
            _uiState.update { it.copy(isNearMe = false) }
            load()
        } else {
            locate()
        }
    }

    fun onCloseClick() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    private fun locate() {
        _uiState.update { it.copy(isLocating = true, locationMessage = null) }
        viewModelScope.launch {
            locationProvider
                .currentLocation()
                .onSuccess { location ->
                    _uiState.update { it.copy(isLocating = false, isNearMe = true, userLocation = location) }
                    load()
                }.onFailure { error ->
                    _uiState.update { it.copy(isLocating = false, locationMessage = error.message) }
                }
        }
    }

    private fun load() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { fetch() }
    }

    private suspend fun fetch() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val state = _uiState.value
        val near = if (state.isNearMe) state.userLocation else null
        kitchenAccessRepository
            .discover(state.searchQuery, near)
            .onSuccess { kitchens ->
                val cozinhas = kitchens.map { it.toListItem() }
                _uiState.update { it.copy(isLoading = false, cozinhas = cozinhas) }
            }.onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message) } }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 400L
        private const val PERMISSION_DENIED = "Permita a localização para ver as cozinhas perto de você."

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    SelectCozinhaViewModel(
                        application.container.kitchenAccessRepository,
                        application.container.authRepository,
                        application.container.locationProvider,
                    )
                }
            }
    }
}

private const val METERS_PER_KM = 1000
private val ptBr: Locale = Locale.forLanguageTag("pt-BR")

internal fun DiscoverKitchenResponse.toListItem() =
    CozinhaListItem(
        id = id,
        name = name,
        location = listOfNotNull(city, state).joinToString(", ").ifBlank { null },
        distance = distanceKm?.toDistanceText(),
        status = myRequestStatus.toCozinhaStatus(),
        latitude = latitude,
        longitude = longitude,
    )

private fun Double.toDistanceText(): String =
    if (this < 1.0) {
        "${(this * METERS_PER_KM).roundToInt()} m"
    } else {
        String.format(ptBr, "%.1f km", this)
    }

private fun KitchenAccessRequestStatus?.toCozinhaStatus(): InventraCozinhaRequestStatus =
    when (this) {
        KitchenAccessRequestStatus.PENDING -> InventraCozinhaRequestStatus.Pendente
        KitchenAccessRequestStatus.REJECTED -> InventraCozinhaRequestStatus.Recusada
        KitchenAccessRequestStatus.APPROVED, null -> InventraCozinhaRequestStatus.Disponivel
    }
