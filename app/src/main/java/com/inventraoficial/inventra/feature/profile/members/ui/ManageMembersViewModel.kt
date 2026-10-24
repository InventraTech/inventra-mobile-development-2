package com.inventraoficial.inventra.feature.profile.members.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestResponse
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.MembersRepository
import com.inventraoficial.inventra.feature.profile.toLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
class ManageMembersViewModel(
    private val membersRepository: MembersRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManageMembersUiState())
    val uiState: StateFlow<ManageMembersUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetryClick() {
        load()
    }

    fun onAcceptClick(request: PendingMemberRequest) {
        if (_uiState.value.processingRequestId != null) return
        _uiState.update { it.copy(processingRequestId = request.id, actionErrorMessage = null) }
        viewModelScope.launch {
            membersRepository
                .approve(request.id)
                .onSuccess { reload() }
                .onFailure { error -> _uiState.update { it.copy(actionErrorMessage = error.message) } }
            _uiState.update { it.copy(processingRequestId = null) }
        }
    }

    fun onRejectClick(request: PendingMemberRequest) {
        _uiState.update { it.copy(rejectTarget = request, rejectReason = "", actionErrorMessage = null) }
    }

    fun onRejectReasonChange(reason: String) {
        _uiState.update { it.copy(rejectReason = reason.take(MAX_REASON_LENGTH)) }
    }

    fun onRejectDismiss() {
        _uiState.update { it.copy(rejectTarget = null, rejectReason = "") }
    }

    fun onRejectConfirm() {
        val state = _uiState.value
        val target = state.rejectTarget ?: return
        val reason = state.rejectReason.trim().ifBlank { null }
        _uiState.update { it.copy(rejectTarget = null, rejectReason = "", processingRequestId = target.id) }
        viewModelScope.launch {
            membersRepository
                .reject(target.id, reason)
                .onSuccess { reload() }
                .onFailure { error -> _uiState.update { it.copy(actionErrorMessage = error.message) } }
            _uiState.update { it.copy(processingRequestId = null) }
        }
    }

    fun onDeactivateClick(member: CozinhaMember) {
        if (member.isCurrentUser) return
        _uiState.update { it.copy(deactivateTarget = member, actionErrorMessage = null) }
    }

    fun onDeactivateDismiss() {
        _uiState.update { it.copy(deactivateTarget = null) }
    }

    fun onDeactivateConfirm() {
        val target = _uiState.value.deactivateTarget ?: return
        _uiState.update { it.copy(deactivateTarget = null, processingMemberId = target.id) }
        viewModelScope.launch {
            membersRepository
                .deactivate(target.id)
                .onSuccess { reload() }
                .onFailure { error -> _uiState.update { it.copy(actionErrorMessage = error.message) } }
            _uiState.update { it.copy(processingMemberId = null) }
        }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            reload()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun reload() {
        val currentUserId = authRepository.currentUser.first()?.id
        val pending = membersRepository.pendingRequests()
        val members = membersRepository.members()
        val error = pending.exceptionOrNull() ?: members.exceptionOrNull()
        _uiState.update {
            it.copy(
                pendingRequests = pending.getOrDefault(emptyList()).map { request -> request.toPendingMemberRequest() },
                members =
                    members
                        .getOrDefault(emptyList())
                        .filter { user -> user.active }
                        .map { user -> user.toCozinhaMember(currentUserId) },
                errorMessage = error?.message,
            )
        }
    }

    companion object {
        private const val MAX_REASON_LENGTH = 255

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    ManageMembersViewModel(
                        membersRepository = application.container.membersRepository,
                        authRepository = application.container.authRepository,
                    )
                }
            }
    }
}

private fun KitchenAccessRequestResponse.toPendingMemberRequest() =
    PendingMemberRequest(
        id = id,
        initials = user.name.initials(),
        name = user.name,
        email = user.email,
        photoUrl = user.photoUrl,
    )

private fun UserResponse.toCozinhaMember(currentUserId: String?): CozinhaMember {
    val isCurrentUser = id == currentUserId
    return CozinhaMember(
        id = id,
        initials = name.initials(),
        name = if (isCurrentUser) "$name (você)" else name,
        roleLabel = profile.accessType.toLabel(),
        isCurrentUser = isCurrentUser,
        photoUrl = photoUrl,
    )
}

internal fun String.initials(): String {
    val words = trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when (words.size) {
        0 -> ""
        1 -> words.first().take(1).uppercase()
        else -> (words.first().take(1) + words.last().take(1)).uppercase()
    }
}
