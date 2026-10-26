package com.inventraoficial.inventra.feature.cozinha.info.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.local.LocationProvider
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.kitchen.KitchenResponse
import com.inventraoficial.inventra.data.remote.dto.kitchen.UpdateKitchenRequest
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.KitchenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
class CozinhaInfoViewModel(
    private val kitchenRepository: KitchenRepository,
    private val authRepository: AuthRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CozinhaInfoUiState())
    val uiState: StateFlow<CozinhaInfoUiState> = _uiState.asStateFlow()

    // Última versão salva: Cancelar volta para ela.
    private var saved: KitchenResponse? = null

    init {
        load()
    }

    fun onRetryClick() = load()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name.take(MAX_NAME_LENGTH), saveError = null) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address.take(MAX_ADDRESS_LENGTH)) }
    }

    fun onCityChange(city: String) {
        _uiState.update { it.copy(city = city.take(MAX_CITY_LENGTH)) }
    }

    fun onStateChange(state: String) {
        if (state.all { it.isLetter() }) {
            _uiState.update { it.copy(state = state.take(STATE_LENGTH).uppercase(), saveError = null) }
        }
    }

    fun onAcceptingRequestsChange(accepting: Boolean) {
        _uiState.update { it.copy(acceptingRequests = accepting) }
    }

    fun onEditClick() {
        if (_uiState.value.canEdit) _uiState.update { it.copy(isEditing = true, savedMessage = null) }
    }

    fun onCancelClick() {
        val kitchen = saved ?: return
        _uiState.update { it.withKitchen(kitchen).copy(isEditing = false, saveError = null, locationMessage = null) }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) {
            onUseMyLocationClick()
        } else {
            _uiState.update { it.copy(locationMessage = PERMISSION_DENIED) }
        }
    }

    fun onUseMyLocationClick() {
        if (_uiState.value.isLocating) return
        _uiState.update { it.copy(isLocating = true, locationMessage = null) }
        viewModelScope.launch {
            locationProvider
                .currentLocation()
                .onSuccess { location -> _uiState.update { it.copy(isLocating = false, newLocation = location) } }
                .onFailure { error -> _uiState.update { it.copy(isLocating = false, locationMessage = error.message) } }
        }
    }

    fun onSaveClick() {
        val state = _uiState.value
        val kitchen = saved
        if (state.isSaving || kitchen == null) return
        val error =
            when {
                state.name.isBlank() -> "Informe o nome da cozinha"
                state.state.isNotEmpty() && state.state.length != STATE_LENGTH -> "A UF tem 2 letras"
                else -> null
            }
        if (error != null) {
            _uiState.update { it.copy(saveError = error) }
            return
        }

        _uiState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            kitchenRepository
                .updateKitchen(kitchen.id, state.toRequest(previous = kitchen))
                .onSuccess { updated -> onSaved(previous = kitchen, updated = updated) }
                .onFailure { failure -> _uiState.update { it.copy(isSaving = false, saveError = failure.message) } }
        }
    }

    private suspend fun onSaved(
        previous: KitchenResponse,
        updated: KitchenResponse,
    ) {
        saved = updated
        // Endereço novo sem pino: o backend busca a localização pelo endereço logo depois de salvar.
        val waitingLocation = updated.address != previous.address && updated.latitude == null
        _uiState.update {
            it.withKitchen(updated).copy(
                isSaving = false,
                isEditing = false,
                savedMessage = if (waitingLocation) SAVED_LOOKING_UP_LOCATION else SAVED,
            )
        }
        // O nome da cozinha também aparece na Home e no Perfil, que leem a sessão.
        if (updated.name != previous.name) authRepository.refreshCurrentUser()
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val kitchenId = user?.kitchenId
            if (kitchenId == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = NO_KITCHEN) }
                return@launch
            }
            kitchenRepository
                .kitchen(kitchenId)
                .onSuccess { kitchen ->
                    saved = kitchen
                    val canEdit = user.accessType == AccessType.SUPERVISOR
                    _uiState.update { it.withKitchen(kitchen).copy(isLoading = false, canEdit = canEdit) }
                }.onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message) } }
        }
    }

    companion object {
        private const val NO_KITCHEN = "Você ainda não faz parte de uma cozinha."
        private const val PERMISSION_DENIED = "Permita a localização para marcar a cozinha no mapa."
        private const val SAVED = "Informações salvas."
        private const val SAVED_LOOKING_UP_LOCATION =
            "Informações salvas. Estamos procurando o endereço novo no mapa; o pino aparece em instantes."

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    CozinhaInfoViewModel(
                        kitchenRepository = application.container.kitchenRepository,
                        authRepository = application.container.authRepository,
                        locationProvider = application.container.locationProvider,
                    )
                }
            }
    }
}

// Limites do backend.
private const val MAX_NAME_LENGTH = 120
private const val MAX_ADDRESS_LENGTH = 255
private const val MAX_CITY_LENGTH = 120
private const val STATE_LENGTH = 2

private fun CozinhaInfoUiState.withKitchen(kitchen: KitchenResponse) =
    copy(
        code = kitchen.code,
        name = kitchen.name,
        address = kitchen.address.orEmpty(),
        city = kitchen.city.orEmpty(),
        state = kitchen.state.orEmpty(),
        acceptingRequests = kitchen.acceptingRequests,
        hasLocation = kitchen.latitude != null && kitchen.longitude != null,
        newLocation = null,
    )

// O endereço só vai quando muda: para o backend, endereço diferente apaga o pino antigo e busca um novo.
// Cidade em branco o backend grava como vazia; UF em branco fica como está (o backend só aceita 2 letras).
private fun CozinhaInfoUiState.toRequest(previous: KitchenResponse) =
    UpdateKitchenRequest(
        name = name.trim(),
        address = address.trim().takeIf { it != previous.address.orEmpty() },
        city = city.trim(),
        state = state.ifEmpty { null },
        latitude = newLocation?.latitude,
        longitude = newLocation?.longitude,
        acceptingRequests = acceptingRequests,
    )
