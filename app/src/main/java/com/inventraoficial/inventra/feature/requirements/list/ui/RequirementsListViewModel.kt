package com.inventraoficial.inventra.feature.requirements.list.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.RequisitionRepository
import com.inventraoficial.inventra.feature.requirements.RequisitionSummary
import com.inventraoficial.inventra.feature.requirements.isVisibleTo
import com.inventraoficial.inventra.feature.requirements.itemsDetails
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

class RequirementsListViewModel(
    private val authRepository: AuthRepository,
    private val requisitionRepository: RequisitionRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(RequirementsListUiState())
    val uiState: StateFlow<RequirementsListUiState> = _uiState.asStateFlow()

    fun onScreenVisible() {
        load()
    }

    fun onRetryClick() {
        load()
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = it.requisitions.isEmpty(), errorMessage = null) }
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val kitchenId = user?.kitchenId
            if (user == null || kitchenId == null) {
                _uiState.update { it.copy(isLoading = false, requisitions = emptyList()) }
                return@launch
            }
            val isSupervisor = user.accessType == AccessType.SUPERVISOR
            _uiState.update { it.copy(isSupervisor = isSupervisor) }

            requisitionRepository
                .requisitions(kitchenId, requesterId = if (isSupervisor) null else user.id)
                .onSuccess { all ->
                    val open =
                        all
                            .filter { it.isOpenFor(user) }
                            .newestFirst()
                            .sortedBy { it.status != RequisitionStatus.DRAFT }
                            .map { requisition ->
                                RequisitionSummary(
                                    id = requisition.id,
                                    title = requisition.titleText(),
                                    subtitle = requisition.subtitleText(zone),
                                    details = requisition.itemsDetails(),
                                    status = requisition.status,
                                )
                            }
                    _uiState.update { it.copy(isLoading = false, requisitions = open) }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    RequirementsListViewModel(
                        authRepository = application.container.authRepository,
                        requisitionRepository = application.container.requisitionRepository,
                    )
                }
            }
    }
}

private fun RequisitionResponse.isOpenFor(user: SessionUser): Boolean =
    when (status) {
        RequisitionStatus.DRAFT -> requester?.id == user.id
        RequisitionStatus.UNDER_REVIEW -> isVisibleTo(user)
        else -> false
    }
