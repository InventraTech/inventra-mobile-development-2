package com.inventraoficial.inventra.feature.cozinha.requestsent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestResponse
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestStatus
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.KitchenAccessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RequestSentViewModel(
    private val kitchenAccessRepository: KitchenAccessRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RequestSentUiState())
    val uiState: StateFlow<RequestSentUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetryClick() = load()

    fun onRefreshClick() {
        val state = _uiState.value
        if (state.isLoading || state.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            checkRequest()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun onChooseAnotherClick() {
        _uiState.update { it.copy(destination = RequestSentDestination.SelectCozinha) }
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(destination = RequestSentDestination.Login) }
        }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            checkRequest()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun checkRequest() {
        kitchenAccessRepository
            .myRequest()
            .onSuccess { request -> handle(request) }
            .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message) } }
    }

    private suspend fun handle(request: KitchenAccessRequestResponse?) {
        if (request == null) {
            _uiState.update { it.copy(destination = RequestSentDestination.SelectCozinha) }
            return
        }
        when (request.status) {
            // Aprovado: atualiza o usuário salvo, que agora já tem cozinha, antes de ir para a Home.
            KitchenAccessRequestStatus.APPROVED ->
                authRepository
                    .refreshCurrentUser()
                    .onSuccess { _uiState.update { it.copy(destination = RequestSentDestination.Home) } }
                    .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message) } }
            KitchenAccessRequestStatus.PENDING ->
                _uiState.update {
                    it.copy(
                        errorMessage = null,
                        cozinhaName = request.kitchen.name,
                        status = RequestSentStatus.Pending,
                        expiresAt = request.expiresAt?.toDateText(),
                        rejectionReason = null,
                    )
                }
            KitchenAccessRequestStatus.REJECTED ->
                _uiState.update {
                    it.copy(
                        errorMessage = null,
                        cozinhaName = request.kitchen.name,
                        status = RequestSentStatus.Rejected,
                        expiresAt = null,
                        rejectionReason = request.reason?.trim()?.ifBlank { null },
                    )
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    RequestSentViewModel(
                        application.container.kitchenAccessRepository,
                        application.container.authRepository,
                    )
                }
            }
    }
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

internal fun String.toDateText(): String? {
    val date =
        runCatching { OffsetDateTime.parse(this).atZoneSameInstant(ZoneId.systemDefault()).toLocalDate() }
            .recoverCatching { LocalDateTime.parse(this).toLocalDate() }
            .getOrNull()
    return date?.format(dateFormatter)
}
