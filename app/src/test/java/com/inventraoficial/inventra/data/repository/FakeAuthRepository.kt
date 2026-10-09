package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.auth.ProfileSummary
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse

/**
 * Repositorio falso para testes: devolve [result] sem tocar na rede e
 * registra os argumentos recebidos para que o teste possa conferi-los.
 */
class FakeAuthRepository(
    var result: Result<UserResponse> = Result.success(fakeUser),
    var hasSession: Boolean = false,
) : AuthRepository {
    var loginCalls = 0
        private set
    var lastEmail: String? = null
        private set
    var lastPassword: String? = null
        private set
    var registerCalls = 0
        private set
    var lastName: String? = null
        private set
    var lastAccessType: AccessType? = null
        private set

    override suspend fun login(
        email: String,
        password: String,
    ): Result<UserResponse> {
        loginCalls++
        lastEmail = email
        lastPassword = password
        return result
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        accessType: AccessType,
    ): Result<UserResponse> {
        registerCalls++
        lastName = name
        lastEmail = email
        lastPassword = password
        lastAccessType = accessType
        return result
    }

    override suspend fun hasSession(): Boolean = hasSession

    var logoutCalls = 0
        private set

    override suspend fun logout() {
        logoutCalls++
        hasSession = false
    }

    companion object {
        val fakeUser =
            UserResponse(
                id = "00000000-0000-0000-0000-000000000001",
                name = "Usuario Teste",
                email = "teste@inventra.com",
                profile = ProfileSummary(id = 1, accessType = AccessType.SUPERVISOR),
                active = true,
                createdAt = "2026-10-03T00:00:00Z",
            )
    }
}
