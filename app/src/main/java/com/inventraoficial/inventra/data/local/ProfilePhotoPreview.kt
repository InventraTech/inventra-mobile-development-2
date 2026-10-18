package com.inventraoficial.inventra.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfilePhotoPreview {
    private val _uri = MutableStateFlow<String?>(null)
    val uri: StateFlow<String?> = _uri.asStateFlow()

    fun show(uri: String) {
        _uri.value = uri
    }

    fun clear() {
        _uri.value = null
    }
}
