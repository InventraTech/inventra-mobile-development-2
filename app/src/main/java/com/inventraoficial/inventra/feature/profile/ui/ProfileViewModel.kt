package com.inventraoficial.inventra.feature.profile.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        // simula o formato que a API deve devolver no futuro
        _uiState.value =
            _uiState.value.copy(
                fullName = "Jones Arakaki Kogake Passos Maldo",
                roleLabel = "Gerente",
                cozinhaName = "Sabor Filial Sul",
            )
    }
}
