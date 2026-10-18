package com.inventraoficial.inventra.feature.profile.photo.ui

data class ProfilePhotoUiState(
    val photoUrl: String? = null,
    val initials: String = "",
    val isSheetVisible: Boolean = false,
    val isUploading: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasPhoto: Boolean
        get() = photoUrl != null
}
