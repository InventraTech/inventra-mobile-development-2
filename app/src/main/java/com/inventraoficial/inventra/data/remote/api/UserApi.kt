package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import retrofit2.http.GET

interface UserApi {
    @GET(PATH)
    suspend fun listMembers(): List<UserResponse>

    @GET("$PATH/me")
    suspend fun me(): UserResponse

    companion object {
        private const val PATH = "users"
    }
}
