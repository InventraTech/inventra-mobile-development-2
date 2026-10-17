package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.alert.AlertResponse
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface AlertApi {
    @GET(PATH)
    suspend fun list(
        @Query("kitchenId") kitchenId: Int,
        @Query("unread") unread: Boolean? = null,
    ): List<AlertResponse>

    @PATCH("$PATH/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: Int,
    ): AlertResponse

    @PATCH("$PATH/read-all")
    suspend fun markAllAsRead(
        @Query("kitchenId") kitchenId: Int,
    )

    companion object {
        private const val PATH = "alerts"
    }
}
