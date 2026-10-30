package com.inventraoficial.inventra.data.remote.api

import retrofit2.http.PATCH
import retrofit2.http.Path

interface ProductStatusApi {
    @PATCH("products/{id}/deactivate")
    suspend fun deactivate(
        @Path("id") productId: Int,
    )
}
