package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.TokenStorage
import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.auth.ErrorResponse
import com.inventraoficial.inventra.data.remote.dto.auth.LoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.LoginResponse
import com.inventraoficial.inventra.data.remote.dto.auth.RegisterRequest
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

interface AuthRepository {
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
}

class NetworkAuthRepository(
    private val authApi: AuthApi,
    private val json: Json,
    private val tokenStorage: TokenStorage,
) : AuthRepository {
    private suspend fun authenticate(call: suspend () -> LoginResponse): Result<UserResponse> =
        try {
            val result = call()

            tokenStorage.saveToken(result.token)
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

    override suspend fun hasSession(): Boolean = tokenStorage.token.first() != null

    override suspend fun logout() {
        tokenStorage.clear()
    }
}
