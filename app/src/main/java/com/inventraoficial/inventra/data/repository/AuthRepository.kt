package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.remote.dto.auth.ErrorResponse
import com.inventraoficial.inventra.data.remote.dto.auth.LoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.UserResponse
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
    ): Result<UserResponse>
}

class NetworkAuthRepository(
    private val authApi: AuthApi,
    private val json: Json,
) : AuthRepository {
    override suspend fun login(
        email: String,
        password: String,
    ): Result<UserResponse> =
        try {
            val result = authApi.login(LoginRequest(email, password))

            Result.success(result.user)
        } catch (e: HttpException) {
            val corpo = e.response()?.errorBody()?.string()
            val erro = corpo?.let { runCatching { json.decodeFromString<ErrorResponse>(it) }.getOrNull() }
            Result.failure(Exception(erro?.detail ?: "Erro inesperado. Tente novamente."))
        } catch (e: IOException) {
            Result.failure(Exception("Não foi possível conectar ao servidor. Verifique sua conexão.", e))
        }
}
