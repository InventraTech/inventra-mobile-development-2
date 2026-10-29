package com.inventraoficial.inventra.feature.profile.details.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.feature.profile.toLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileDetailsViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileDetailsUiState())
    val uiState: StateFlow<ProfileDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update {
                    it.copy(
                        fullName = user?.name.orEmpty(),
                        email = user?.email.orEmpty(),
                        roleLabel = user?.accessType?.toLabel().orEmpty(),
                        cozinhaName = user?.kitchenName ?: "Nenhuma cozinha vinculada",
                        leaveKitchen =
                            it.leaveKitchen.copy(
                                canLeave = user?.kitchenId != null,
                                isSupervisor = user?.accessType == AccessType.SUPERVISOR,
                            ),
                    )
                }
            }
        }
    }

    fun onLogoutClick() {
        _uiState.update { it.copy(isLogoutDialogVisible = true) }
    }

    fun onLogoutDismiss() {
        _uiState.update { it.copy(isLogoutDialogVisible = false) }
    }

    fun onLogoutConfirm() {
        _uiState.update { it.copy(isLogoutDialogVisible = false) }
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    fun onLeaveKitchenClick() = updateLeave { it.copy(isDialogVisible = true, errorMessage = null) }

    fun onLeaveKitchenDismiss() = updateLeave { it.copy(isDialogVisible = false) }

    fun onLeaveKitchenConfirm() {
        if (_uiState.value.leaveKitchen.isLeaving) return
        updateLeave { it.copy(isDialogVisible = false, isLeaving = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository
                .leaveKitchen()
                .onSuccess { updateLeave { it.copy(isLeaving = false, hasLeft = true) } }
                .onFailure { error -> updateLeave { it.copy(isLeaving = false, errorMessage = error.message) } }
        }
    }

    private fun updateLeave(transform: (LeaveKitchenState) -> LeaveKitchenState) {
        _uiState.update { it.copy(leaveKitchen = transform(it.leaveKitchen)) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    ProfileDetailsViewModel(application.container.authRepository)
                }
            }
    }
}
