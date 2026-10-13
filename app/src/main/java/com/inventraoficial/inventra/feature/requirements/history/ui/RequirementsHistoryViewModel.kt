package com.inventraoficial.inventra.feature.requirements.history.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.RequisitionRepository
import com.inventraoficial.inventra.feature.requirements.RequisitionSummary
import com.inventraoficial.inventra.feature.requirements.isVisibleTo
import com.inventraoficial.inventra.feature.requirements.newestFirst
import com.inventraoficial.inventra.feature.requirements.subtitleText
import com.inventraoficial.inventra.feature.requirements.titleText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId

class RequirementsHistoryViewModel(
    private val authRepository: AuthRepository,
    private val requisitionRepository: RequisitionRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(RequirementsHistoryUiState())
    val uiState: StateFlow<RequirementsHistoryUiState> = _uiState.asStateFlow()

    private var processed: List<RequisitionResponse> = emptyList()

    fun onScreenVisible() {
        load()
    }

    fun onRetryClick() {
        load()
    }

    fun onFilterSelect(filter: HistoryFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        refreshVisible()
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = processed.isEmpty(), errorMessage = null) }
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val kitchenId = user?.kitchenId
            if (user == null || kitchenId == null) {
                processed = emptyList()
                _uiState.update { it.copy(isLoading = false) }
                refreshVisible()
                return@launch
            }
            val requesterId = if (user.accessType == AccessType.SUPERVISOR) null else user.id
            requisitionRepository
                .requisitions(kitchenId, requesterId = requesterId)
                .onSuccess { all ->
                    processed =
                        all
                            .filter { it.status !in OPEN_STATUSES && it.isVisibleTo(user) }
                            .newestFirst()
                    _uiState.update { it.copy(isLoading = false) }
                    refreshVisible()
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun refreshVisible() {
        val status = _uiState.value.selectedFilter.status
        val visible =
            processed
                .filter { status == null || it.status == status }
                .map { requisition ->
                    RequisitionSummary(
                        id = requisition.id,
                        title = requisition.titleText(),
                        subtitle = requisition.subtitleText(zone),
                        details = requisition.reason?.let { "Motivo: $it" } ?: requisition.origin.orEmpty(),
                        status = requisition.status,
                    )
                }
        _uiState.update { it.copy(requisitions = visible) }
    }

    companion object {
        private val OPEN_STATUSES = setOf(RequisitionStatus.DRAFT, RequisitionStatus.UNDER_REVIEW)

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    RequirementsHistoryViewModel(
                        authRepository = application.container.authRepository,
                        requisitionRepository = application.container.requisitionRepository,
                    )
                }
            }
    }
}
