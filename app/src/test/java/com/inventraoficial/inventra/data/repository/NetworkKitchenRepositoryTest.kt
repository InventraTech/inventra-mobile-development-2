package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.KitchenApi
import com.inventraoficial.inventra.data.remote.dto.kitchen.CreateKitchenRequest
import com.inventraoficial.inventra.data.remote.dto.kitchen.DiscoverKitchenPage
import com.inventraoficial.inventra.data.remote.dto.kitchen.KitchenResponse
import com.inventraoficial.inventra.data.remote.dto.kitchen.UpdateKitchenRequest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkKitchenRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }

    private class FakeKitchenApi(
        private val respond: () -> KitchenResponse,
    ) : KitchenApi {
        var lastRequest: CreateKitchenRequest? = null
            private set
        var lastFind: Int? = null
            private set
        var lastUpdate: Pair<Int, UpdateKitchenRequest>? = null
            private set

        override suspend fun create(request: CreateKitchenRequest): KitchenResponse {
            lastRequest = request
            return respond()
        }

        override suspend fun discover(
            query: String?,
            latitude: Double?,
            longitude: Double?,
            size: Int,
        ): DiscoverKitchenPage = error("nao usado")

        override suspend fun find(id: Int): KitchenResponse {
            lastFind = id
            return respond()
        }

        override suspend fun update(
            id: Int,
            request: UpdateKitchenRequest,
        ): KitchenResponse {
            lastUpdate = id to request
            return respond()
        }
    }

    @Test
    fun `createKitchen envia nome e endereco e devolve a cozinha criada`() =
        runTest {
            val api = FakeKitchenApi { FakeKitchenRepository.fakeKitchen }
            val repository = NetworkKitchenRepository(api, json)

            val result = repository.createKitchen("Sabor Filial Sul", "Av. das Indústrias, 450")

            assertEquals(FakeKitchenRepository.fakeKitchen, result.getOrNull())
            assertEquals(CreateKitchenRequest("Sabor Filial Sul", "Av. das Indústrias, 450"), api.lastRequest)
        }

    @Test
    fun `createKitchen sem conexao devolve falha`() =
        runTest {
            val repository = NetworkKitchenRepository(FakeKitchenApi { throw IOException("sem sinal") }, json)

            val result = repository.createKitchen("Sabor Filial Sul", null)

            assertTrue(result.isFailure)
        }

    @Test
    fun `kitchen busca a cozinha pelo id`() =
        runTest {
            val api = FakeKitchenApi { FakeKitchenRepository.fakeKitchen }
            val repository = NetworkKitchenRepository(api, json)

            val result = repository.kitchen(7)

            assertEquals(FakeKitchenRepository.fakeKitchen, result.getOrNull())
            assertEquals(7, api.lastFind)
        }

    @Test
    fun `updateKitchen envia o id e as mudancas`() =
        runTest {
            val api = FakeKitchenApi { FakeKitchenRepository.fakeKitchen }
            val repository = NetworkKitchenRepository(api, json)
            val request = UpdateKitchenRequest(name = "Sabor Filial Norte", acceptingRequests = false)

            val result = repository.updateKitchen(7, request)

            assertTrue(result.isSuccess)
            assertEquals(7 to request, api.lastUpdate)
        }
}
