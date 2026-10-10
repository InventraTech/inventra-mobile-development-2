package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.SessionStorage
import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.local.toSessionUser
import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.remote.api.UserApi
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.auth.ErrorResponse
import com.inventraoficial.inventra.data.remote.dto.auth.LoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.LoginResponse
import com.inventraoficial.inventra.data.remote.dto.auth.RefreshTokenRequest
import com.inventraoficial.inventra.data.remote.dto.auth.RegisterRequest
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

interface AuthRepository {
    val currentUser: Flow<SessionUser?>

    suspend fun login(
        email: String,
        password: String,
    ): Result<UserResponse>

    suspend fun register(
        name: String,
        email: String,
        password: String,
        accessType: AccessType,
    ): Result<UserResponse>

    suspend fun hasSession(): Boolean

    suspend fun logout()

    suspend fun refreshCurrentUser(): Result<Unit>
}

class NetworkAuthRepository(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val json: Json,
    private val sessionStorage: SessionStorage,
) : AuthRepository {
    override val currentUser: Flow<SessionUser?> = sessionStorage.user

    private suspend fun authenticate(call: suspend () -> LoginResponse): Result<UserResponse> =
        try {
            val result = call()

            sessionStorage.saveSession(result.token, result.user.toSessionUser(), result.refreshToken)
            Result.success(result.user)
        } catch (e: HttpException) {
            val corpo = e.response()?.errorBody()?.string()
            val erro = corpo?.let { runCatching { json.decodeFromString<ErrorResponse>(it) }.getOrNull() }
            Result.failure(Exception(erro?.detail ?: "Erro inesperado. Tente novamente."))
        } catch (e: IOException) {
            Result.failure(Exception("Não foi possível conectar ao servidor. Verifique sua conexão.", e))
        } catch (e: SerializationException) {
            Result.failure(Exception("Resposta inesperada do servidor. Tente novamente.", e))
        }

    override suspend fun login(
        email: String,
        password: String,
    ): Result<UserResponse> =
        authenticate {
            authApi.login(LoginRequest(email, password))
        }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        accessType: AccessType,
    ): Result<UserResponse> =
        authenticate {
            authApi.register(RegisterRequest(name, email, password, accessType))
        }

    override suspend fun hasSession(): Boolean = sessionStorage.token.first() != null

    override suspend fun logout() {
        val refreshToken = sessionStorage.refreshToken.first()
        if (refreshToken != null) {
            try {
                authApi.logout(RefreshTokenRequest(refreshToken))
            } catch (ignored: HttpException) {
                // O servidor recusou (carteirinha ja vencida ou invalida): nada a cancelar.
            } catch (ignored: IOException) {
                // Sem conexao: a carteirinha vence sozinha no servidor.
            }
        }
        sessionStorage.clear()
    }

    override suspend fun refreshCurrentUser(): Result<Unit> =
        try {
            val currentUser = userApi.me().toSessionUser()
            sessionStorage.updateUser(currentUser)
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: SerializationException) {
            Result.failure(e)
        }
}
