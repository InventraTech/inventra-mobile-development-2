package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.PreListApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.prelist.PreListResponse
import com.inventraoficial.inventra.data.remote.dto.prelist.PreListSummaryResponse
import kotlinx.serialization.json.Json

interface PreListRepository {
    suspend fun preLists(): Result<List<PreListSummaryResponse>>

    suspend fun preList(id: Int): Result<PreListResponse>
}

class NetworkPreListRepository(
    private val preListApi: PreListApi,
    private val json: Json,
) : PreListRepository {
    override suspend fun preLists() = apiCall(json) { preListApi.list() }

    override suspend fun preList(id: Int) = apiCall(json) { preListApi.find(id) }
}
