package com.inventraoficial.inventra.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StartViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _startScreen = MutableStateFlow<Screen?>(null)
    val startScreen: StateFlow<Screen?> = _startScreen.asStateFlow()

    init {
        viewModelScope.launch {
            if (authRepository.hasSession()) {
                _startScreen.value = Screen.Home
            } else {
                _startScreen.value = Screen.Login
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    StartViewModel(application.container.authRepository)
                }
            }
    }
}
