package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.PreListApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.prelist.PreListResponse
import kotlinx.serialization.json.Json

interface PreListRepository {
    suspend fun preLists(): Result<List<PreListResponse>>

    suspend fun preList(id: Int): Result<PreListResponse>
}

class NetworkPreListRepository(
    private val preListApi: PreListApi,
    private val json: Json,
) : PreListRepository {
    override suspend fun preLists() = apiCall(json) { preListApi.list() }

    override suspend fun preList(id: Int) = apiCall(json) { preListApi.find(id) }
}
