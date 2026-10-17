package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.KitchenApi
import com.inventraoficial.inventra.data.remote.dto.kitchen.CreateKitchenRequest
import com.inventraoficial.inventra.data.remote.dto.kitchen.DiscoverKitchenPage
import com.inventraoficial.inventra.data.remote.dto.kitchen.KitchenResponse
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
}
