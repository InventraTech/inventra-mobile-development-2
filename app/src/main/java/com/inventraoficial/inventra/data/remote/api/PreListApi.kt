package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.prelist.PreListResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PreListApi {
    @GET(PATH)
    suspend fun list(): List<PreListResponse>

    @GET("$PATH/{id}")
    suspend fun find(
        @Path("id") id: Int,
    ): PreListResponse

    companion object {
        private const val PATH = "pre-lists"
    }
}
