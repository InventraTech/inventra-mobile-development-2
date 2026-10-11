package com.inventraoficial.inventra.feature.profile.ui

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

class ProfileViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update {
                    it.copy(
                        fullName = user?.name.orEmpty(),
                        roleLabel = user?.accessType?.toLabel().orEmpty(),
                        cozinhaName = user?.kitchenName ?: "Nenhuma cozinha vinculada",
                        canManageMembers = user?.accessType == AccessType.SUPERVISOR && user.kitchenId != null,
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    ProfileViewModel(application.container.authRepository)
                }
            }
    }
}
