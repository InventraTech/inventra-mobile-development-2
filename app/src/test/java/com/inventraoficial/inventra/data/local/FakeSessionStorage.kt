package com.inventraoficial.inventra.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** SessionStorage em memoria para testes: nada e gravado em disco. */
class FakeSessionStorage(
    initialToken: String? = null,
    initialUser: SessionUser? = null,
) : SessionStorage {
    private val _token = MutableStateFlow(initialToken)
    private val _user = MutableStateFlow(initialUser)

    override val token: Flow<String?> = _token
    override val user: Flow<SessionUser?> = _user

    /** Valores atuais, para o teste conferir sem precisar coletar os Flows. */
    val currentToken: String?
        get() = _token.value
    val currentUser: SessionUser?
        get() = _user.value

    override suspend fun saveSession(
        token: String,
        user: SessionUser,
    ) {
        _token.value = token
        _user.value = user
    }

    override suspend fun updateUser(user: SessionUser) {
        _user.value = user
    }

    override suspend fun clear() {
        _token.value = null
        _user.value = null
    }
}
