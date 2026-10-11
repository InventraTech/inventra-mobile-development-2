package com.inventraoficial.inventra.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSessionStorage(
    initialToken: String? = null,
    initialUser: SessionUser? = null,
    initialRefreshToken: String? = null,
) : SessionStorage {
    private val _token = MutableStateFlow(initialToken)
    private val _user = MutableStateFlow(initialUser)
    private val _refreshToken = MutableStateFlow(initialRefreshToken)

    override val token: Flow<String?> = _token
    override val user: Flow<SessionUser?> = _user
    override val refreshToken: Flow<String?> = _refreshToken

    val currentToken: String?
        get() = _token.value
    val currentUser: SessionUser?
        get() = _user.value
    val currentRefreshToken: String?
        get() = _refreshToken.value

    override suspend fun saveSession(
        token: String,
        user: SessionUser,
        refreshToken: String,
    ) {
        _token.value = token
        _user.value = user
        _refreshToken.value = refreshToken
    }

    override suspend fun updateUser(user: SessionUser) {
        _user.value = user
    }

    override suspend fun clear() {
        _token.value = null
        _user.value = null
        _refreshToken.value = null
    }
}
