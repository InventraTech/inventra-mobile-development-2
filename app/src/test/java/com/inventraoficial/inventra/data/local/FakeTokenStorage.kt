package com.inventraoficial.inventra.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** TokenStorage em memoria para testes: nada e gravado em disco. */
class FakeTokenStorage(
    initialToken: String? = null,
) : TokenStorage {
    private val _token = MutableStateFlow(initialToken)
    override val token: Flow<String?> = _token

    /** Valor atual, para o teste conferir sem precisar coletar o Flow. */
    val currentToken: String?
        get() = _token.value

    override suspend fun saveToken(token: String) {
        _token.value = token
    }

    override suspend fun clear() {
        _token.value = null
    }
}
