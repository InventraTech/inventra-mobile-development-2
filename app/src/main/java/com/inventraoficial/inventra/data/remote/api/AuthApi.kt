package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.auth.ForgotPasswordRequest
import com.inventraoficial.inventra.data.remote.dto.auth.GoogleLoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.LoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.LoginResponse
import com.inventraoficial.inventra.data.remote.dto.auth.RefreshTokenRequest
import com.inventraoficial.inventra.data.remote.dto.auth.RegisterRequest
import com.inventraoficial.inventra.data.remote.dto.auth.ResetPasswordRequest
import com.inventraoficial.inventra.data.remote.dto.auth.ResetTokenResponse
import com.inventraoficial.inventra.data.remote.dto.auth.VerifyResetCodeRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("$PATH/login")
    suspend fun login(
        @Body loginRequest: LoginRequest,
    ): LoginResponse

    @POST("$PATH/register")
    suspend fun register(
        @Body registerRequest: RegisterRequest,
    ): LoginResponse

    @POST("$PATH/google")
    suspend fun loginWithGoogle(
        @Body googleLoginRequest: GoogleLoginRequest,
    ): LoginResponse

    @POST("$PATH/refresh")
    suspend fun refresh(
        @Body refreshRequest: RefreshTokenRequest,
    ): LoginResponse

    @POST("$PATH/logout")
    suspend fun logout(
        @Body refreshRequest: RefreshTokenRequest,
    )

    @POST("$PATH/password/forgot")
    suspend fun forgotPassword(
        @Body forgotPasswordRequest: ForgotPasswordRequest,
    )

    @POST("$PATH/password/verify")
    suspend fun verifyResetCode(
        @Body verifyResetCodeRequest: VerifyResetCodeRequest,
    ): ResetTokenResponse

    @POST("$PATH/password/reset")
    suspend fun resetPassword(
        @Body resetPasswordRequest: ResetPasswordRequest,
    )

    companion object {
        private const val PATH = "auth"
    }
}
