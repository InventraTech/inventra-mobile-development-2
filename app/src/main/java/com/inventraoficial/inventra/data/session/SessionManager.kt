package com.inventraoficial.inventra.data.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SessionManager {
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}
