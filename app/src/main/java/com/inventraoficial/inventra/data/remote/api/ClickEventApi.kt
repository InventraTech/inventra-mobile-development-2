package com.inventraoficial.inventra.data.remote.api

import com.inventraoficial.inventra.data.remote.dto.event.RecordUiClicksRequest
import com.inventraoficial.inventra.data.remote.dto.event.UiClickBatchResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ClickEventApi {
    @POST(PATH)
    suspend fun record(
        @Body request: RecordUiClicksRequest,
    ): UiClickBatchResponse

    companion object {
        private const val PATH = "events/clicks"
    }
}
