package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.ApiException
import com.inventraoficial.inventra.data.remote.api.AuthApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.auth.ForgotPasswordRequest
import com.inventraoficial.inventra.data.remote.dto.auth.ResetPasswordRequest
import com.inventraoficial.inventra.data.remote.dto.auth.VerifyResetCodeRequest
import kotlinx.serialization.json.Json

interface PasswordResetRepository {
    suspend fun requestCode(email: String): Result<Unit>

    suspend fun verifyCode(
        email: String,
        code: String,
    ): Result<String>

    suspend fun resetPassword(
        resetToken: String,
        newPassword: String,
    ): Result<Unit>
}

class NetworkPasswordResetRepository(
    private val authApi: AuthApi,
    private val json: Json,
) : PasswordResetRepository {
    override suspend fun requestCode(email: String): Result<Unit> =
        apiCall(json) {
            authApi.forgotPassword(ForgotPasswordRequest(email))
        }

    override suspend fun verifyCode(
        email: String,
        code: String,
    ): Result<String> = apiCall(json) { authApi.verifyResetCode(VerifyResetCodeRequest(email, code)).resetToken }

    override suspend fun resetPassword(
        resetToken: String,
        newPassword: String,
    ): Result<Unit> {
        val result = apiCall(json) { authApi.resetPassword(ResetPasswordRequest(resetToken, newPassword)) }
        val error = result.exceptionOrNull() as? ApiException
        return if (error?.error?.code == INVALID_RESET_TOKEN) {
            Result.failure(ResetTokenExpiredException(error.message.orEmpty()))
        } else {
            result
        }
    }

    private companion object {
        const val INVALID_RESET_TOKEN = "INVALID_RESET_TOKEN"
    }
}

class ResetTokenExpiredException(
    message: String,
) : Exception(message)
