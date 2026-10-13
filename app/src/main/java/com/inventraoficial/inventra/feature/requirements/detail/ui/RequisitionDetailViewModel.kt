package com.inventraoficial.inventra.feature.requirements.detail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionItemResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.RequisitionRepository
import com.inventraoficial.inventra.feature.requirements.createdDateText
import com.inventraoficial.inventra.feature.requirements.currencyText
import com.inventraoficial.inventra.feature.requirements.quantityText
import com.inventraoficial.inventra.feature.requirements.titleText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId

class RequisitionDetailViewModel(
    private val requisitionId: Int,
    private val authRepository: AuthRepository,
    private val requisitionRepository: RequisitionRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(RequisitionDetailUiState())
    val uiState: StateFlow<RequisitionDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetryClick() {
        load()
    }

    fun onApproveClick() = openDialog(PendingAction.Approve)

    fun onRejectClick() = openDialog(PendingAction.Reject)

    fun onCancelClick() = openDialog(PendingAction.Cancel)

    fun onReasonChange(text: String) {
        _uiState.update { it.copy(reasonText = text.take(MAX_REASON_LENGTH), actionErrorMessage = null) }
    }

    fun onDialogDismiss() {
        _uiState.update { it.copy(pendingAction = null, reasonText = "", actionErrorMessage = null) }
    }

    fun onDialogConfirm() {
        val state = _uiState.value
        val action = state.pendingAction
        if (action == null || state.isProcessing) return
        val reason = state.reasonText.trim()
        if (action != PendingAction.Approve && reason.isEmpty()) {
            _uiState.update { it.copy(actionErrorMessage = "Informe o motivo.") }
            return
        }
        _uiState.update { it.copy(isProcessing = true, actionErrorMessage = null) }
        viewModelScope.launch {
            val result =
                when (action) {
                    PendingAction.Approve -> requisitionRepository.approve(requisitionId)
                    PendingAction.Reject -> requisitionRepository.reject(requisitionId, reason)
                    PendingAction.Cancel -> requisitionRepository.cancel(requisitionId, reason)
                }
            result
                .onSuccess {
                    _uiState.update { it.copy(isProcessing = false, pendingAction = null, isFinished = true) }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            pendingAction = null,
                            reasonText = "",
                            actionErrorMessage = error.message,
                        )
                    }
                }
        }
    }

    private fun openDialog(action: PendingAction) {
        _uiState.update { it.copy(pendingAction = action, reasonText = "", actionErrorMessage = null) }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val kitchenId = user?.kitchenId
            if (user == null || kitchenId == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = NOT_FOUND) }
                return@launch
            }
            val requisition =
                requisitionRepository
                    .requisitions(kitchenId)
                    .getOrElse { error ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                        return@launch
                    }.firstOrNull { it.id == requisitionId }
            if (requisition == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = NOT_FOUND) }
                return@launch
            }
            val items =
                requisitionRepository.items(requisitionId).getOrElse { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                    return@launch
                }
            _uiState.update { it.withRequisition(requisition, items, user) }
        }
    }

    private fun RequisitionDetailUiState.withRequisition(
        requisition: RequisitionResponse,
        items: List<RequisitionItemResponse>,
        user: SessionUser,
    ): RequisitionDetailUiState {
        val isUnderReview = requisition.status == RequisitionStatus.UNDER_REVIEW
        val pricedItems = items.filter { it.estimatedPrice != null }
        return copy(
            title = requisition.titleText(),
            status = requisition.status,
            type = requisition.type,
            requesterName = requisition.requester?.name.orEmpty(),
            dateText = requisition.createdDateText(zone),
            origin = requisition.origin?.takeIf { it.isNotBlank() },
            reason = requisition.reason?.takeIf { it.isNotBlank() },
            items = items.map { it.toUi() },
            totalText =
                pricedItems
                    .takeIf { it.isNotEmpty() }
                    ?.sumOf { it.quantity * (it.estimatedPrice ?: 0.0) }
                    ?.currencyText(),
            canReview = isUnderReview && user.accessType == AccessType.SUPERVISOR,
            canCancel = isUnderReview && requisition.requester?.id == user.id,
            isLoading = false,
        )
    }

    companion object {
        private const val MAX_REASON_LENGTH = 255
        private const val NOT_FOUND = "Requisição não encontrada."

        fun factory(requisitionId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    RequisitionDetailViewModel(
                        requisitionId = requisitionId,
                        authRepository = application.container.authRepository,
                        requisitionRepository = application.container.requisitionRepository,
                    )
                }
            }
    }
}

private fun RequisitionItemResponse.toUi() =
    RequisitionItemUi(
        name = product.name,
        quantityText = "Qtd. ${quantity.quantityText()}",
        priceText = estimatedPrice?.let { "Preço estimado ${it.currencyText()}" },
        supplier = suggestedSupplier?.legalName,
        note = note?.takeIf { it.isNotBlank() },
    )
