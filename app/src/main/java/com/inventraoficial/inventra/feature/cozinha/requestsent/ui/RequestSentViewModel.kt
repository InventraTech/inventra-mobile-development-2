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
import com.inventraoficial.inventra.feature.cozinha.select.ui.toListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class RequestSentViewModel(
    private val kitchenAccessRepository: KitchenAccessRepository,
    private val authRepository: AuthRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(RequestSentUiState())
    val uiState: StateFlow<RequestSentUiState> = _uiState.asStateFlow()

    private var currentKitchenId: Int? = null

    init {
        load()
    }

    fun onRetryClick() = load()

    fun onRefreshClick() {
        val state = _uiState.value
        if (state.isLoading || state.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            if (checkRequest(silent = false)) loadOtherRequests()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    // Chamado ao voltar para o app e de tempos em tempos com a tela aberta. Só confere o pedido atual
    // (a busca de cozinhas tem limite por minuto) e, se falhar, mantém o que já está na tela.
    fun onAutoRefresh() {
        val state = _uiState.value
        if (state.isLoading || state.isRefreshing || state.destination != null) return
        viewModelScope.launch { checkRequest(silent = true) }
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
            val loaded = checkRequest(silent = false)
            _uiState.update { it.copy(isLoading = false) }
            if (loaded) loadOtherRequests()
        }
    }

    // Devolve true quando há um pedido pendente ou recusado na tela.
    private suspend fun checkRequest(silent: Boolean): Boolean =
        kitchenAccessRepository
            .myRequest()
            .onFailure { error -> if (!silent) _uiState.update { it.copy(errorMessage = error.message) } }
            .map { request -> handle(request) }
            .getOrDefault(false)

    private suspend fun handle(request: KitchenAccessRequestResponse?): Boolean {
        if (request == null) {
            _uiState.update { it.copy(destination = RequestSentDestination.SelectCozinha) }
            return false
        }
        currentKitchenId = request.kitchen.id
        when (request.status) {
            // Aprovado: atualiza o usuário salvo, que agora já tem cozinha, antes de ir para a Home.
            KitchenAccessRequestStatus.APPROVED ->
                authRepository
                    .refreshCurrentUser()
                    .onSuccess { _uiState.update { it.copy(destination = RequestSentDestination.Home) } }
                    .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message) } }
            KitchenAccessRequestStatus.PENDING -> {
                val expiration = request.expiresAt?.toLocalDate(clock.zone)
                _uiState.update {
                    it.copy(
                        errorMessage = null,
                        cozinhaName = request.kitchen.name,
                        status = RequestSentStatus.Pending,
                        sentAt = request.createdAt.toLocalDate(clock.zone)?.format(dateFormatter),
                        expiresAt = expiration?.format(dateFormatter),
                        expiresIn = expiration?.let { date -> expiresInText(date) },
                        rejectionReason = null,
                    )
                }
            }
            KitchenAccessRequestStatus.REJECTED ->
                _uiState.update {
                    it.copy(
                        errorMessage = null,
                        cozinhaName = request.kitchen.name,
                        status = RequestSentStatus.Rejected,
                        sentAt = request.createdAt.toLocalDate(clock.zone)?.format(dateFormatter),
                        expiresAt = null,
                        expiresIn = null,
                        rejectionReason = request.reason?.trim()?.ifBlank { null },
                    )
                }
        }
        return request.status != KitchenAccessRequestStatus.APPROVED
    }

    // A busca de cozinhas traz o status do pedido do usuário em cada uma; fica de fora a do pedido atual.
    // Se falhar, a lista só não aparece: o pedido atual continua na tela.
    private suspend fun loadOtherRequests() {
        kitchenAccessRepository.discover(query = "").onSuccess { kitchens ->
            val others =
                kitchens
                    .filter { it.myRequestStatus != null && it.id != currentKitchenId }
                    .map { it.toListItem() }
            _uiState.update { it.copy(otherRequests = others) }
        }
    }

    private fun expiresInText(date: LocalDate): String =
        when (val days = ChronoUnit.DAYS.between(LocalDate.now(clock), date)) {
            in Long.MIN_VALUE..0L -> "vence hoje"
            1L -> "vence amanhã"
            else -> "vence em $days dias"
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

private fun String.toLocalDate(zone: ZoneId): LocalDate? =
    runCatching { OffsetDateTime.parse(this).atZoneSameInstant(zone).toLocalDate() }
        .recoverCatching { LocalDateTime.parse(this).toLocalDate() }
        .getOrNull()
