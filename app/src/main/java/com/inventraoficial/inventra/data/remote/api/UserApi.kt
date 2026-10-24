package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface UserApi {
    @GET(PATH)
    suspend fun listMembers(): List<UserResponse>

    @GET("$PATH/me")
    suspend fun me(): UserResponse

    @Multipart
    @PUT("$PATH/me/photo")
    suspend fun updatePhoto(
        @Part file: MultipartBody.Part,
    ): UserResponse

    @DELETE("$PATH/me/photo")
    suspend fun removePhoto(): UserResponse

    @PATCH("$PATH/{id}/deactivate")
    suspend fun deactivate(
        @Path("id") id: String,
    )

    companion object {
        private const val PATH = "users"
    }
}
