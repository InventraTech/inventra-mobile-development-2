package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.auth.LoginRequest
import com.inventraoficial.inventra.data.remote.dto.auth.LoginResponse
import com.inventraoficial.inventra.data.remote.dto.auth.RefreshTokenRequest
import com.inventraoficial.inventra.data.remote.dto.auth.RegisterRequest
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

    @POST("$PATH/refresh")
    suspend fun refresh(
        @Body refreshRequest: RefreshTokenRequest,
    ): LoginResponse

    @POST("$PATH/logout")
    suspend fun logout(
        @Body refreshRequest: RefreshTokenRequest,
    )

    companion object {
        private const val PATH = "auth"
    }
}
