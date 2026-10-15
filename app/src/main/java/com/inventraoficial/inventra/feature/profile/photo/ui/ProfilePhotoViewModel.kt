package com.inventraoficial.inventra.feature.profile.photo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.local.ImageCompressor
import com.inventraoficial.inventra.data.local.ProfilePhotoPreview
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.feature.profile.members.ui.initials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfilePhotoViewModel(
    private val authRepository: AuthRepository,
    private val imageCompressor: ImageCompressor,
    private val preview: ProfilePhotoPreview,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfilePhotoUiState())
    val uiState: StateFlow<ProfilePhotoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(authRepository.currentUser, preview.uri) { user, localUri ->
                (localUri ?: user?.photoUrl) to user?.name.orEmpty().initials()
            }.collect { (photoUrl, initials) ->
                _uiState.update { it.copy(photoUrl = photoUrl, initials = initials) }
            }
        }
    }

    fun onAvatarClick() {
        if (_uiState.value.isUploading) return
        _uiState.update { it.copy(isSheetVisible = true, errorMessage = null) }
    }

    fun onSheetDismiss() {
        _uiState.update { it.copy(isSheetVisible = false) }
    }

    fun onPhotoPicked(uri: String?) {
        _uiState.update { it.copy(isSheetVisible = false) }
        if (uri == null) return

        preview.show(uri)
        _uiState.update { it.copy(isUploading = true, errorMessage = null) }
        viewModelScope.launch {
            val result =
                imageCompressor
                    .compress(uri)
                    .fold(
                        onSuccess = { image -> authRepository.updatePhoto(image) },
                        onFailure = { error -> Result.failure(error) },
                    )
            if (result.isSuccess) preview.clear()
            finish(result)
        }
    }

    fun onRemoveClick() {
        preview.clear()
        _uiState.update { it.copy(isSheetVisible = false, isUploading = true, errorMessage = null) }
        viewModelScope.launch { finish(authRepository.removePhoto()) }
    }

    private fun finish(result: Result<Unit>) {
        _uiState.update { it.copy(isUploading = false, errorMessage = result.exceptionOrNull()?.message) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    ProfilePhotoViewModel(
                        authRepository = application.container.authRepository,
                        imageCompressor = application.container.imageCompressor,
                        preview = application.container.profilePhotoPreview,
                    )
                }
            }
    }
}
