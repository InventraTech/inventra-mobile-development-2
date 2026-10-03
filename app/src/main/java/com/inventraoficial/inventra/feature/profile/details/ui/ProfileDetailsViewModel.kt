package com.inventraoficial.inventra.feature.profile.details.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileDetailsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileDetailsUiState())
    val uiState: StateFlow<ProfileDetailsUiState> = _uiState.asStateFlow()

    init {
        loadProfileDetails()
    }

    private fun loadProfileDetails() {
        // simula o formato que a API deve devolver no futuro
        _uiState.value =
            _uiState.value.copy(
                fullName = "Jones Arakaki Kogake Passos Maldo",
                roleLabel = "Gerente",
                cozinhaName = "Sabor Filial Sul",
                login = "jones.silva",
            )
    }
}
