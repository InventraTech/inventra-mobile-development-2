package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.SupplierApi
import com.inventraoficial.inventra.data.remote.dto.supplier.SupplierResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkSupplierRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val seara = FakeSupplierRepository.supplier(1, "Seara Alimentos", rating = 5)

    private class FakeSupplierApi(
        private val suppliers: List<SupplierResponse>,
        private val error: Exception? = null,
    ) : SupplierApi {
        var lastFindId: Int? = null
            private set
        var lastKitchenId: Int? = null
            private set

        override suspend fun list(kitchenId: Int): List<SupplierResponse> {
            error?.let { throw it }
            lastKitchenId = kitchenId
            return suppliers
        }

        override suspend fun find(id: Int): SupplierResponse {
            error?.let { throw it }
            lastFindId = id
            return suppliers.first { it.id == id }
        }

        var activeCalls = 0
            private set

        override suspend fun listActive(): List<SupplierResponse> {
            error?.let { throw it }
            activeCalls++
            return suppliers
        }
    }

    @Test
    fun `allSuppliers lista todos os ativos, sem filtrar pela cozinha`() =
        runTest {
            val api = FakeSupplierApi(listOf(seara))
            val repository = NetworkSupplierRepository(api, json)

            val result = repository.allSuppliers()

            assertEquals(listOf(seara), result.getOrNull())
            assertEquals(1, api.activeCalls)
            assertEquals(null, api.lastKitchenId)
        }

    @Test
    fun `suppliers lista os fornecedores da cozinha`() =
        runTest {
            val api = FakeSupplierApi(listOf(seara))
            val repository = NetworkSupplierRepository(api, json)

            val result = repository.suppliers(kitchenId = 7)

            assertEquals(listOf(seara), result.getOrNull())
            assertEquals(7, api.lastKitchenId)
        }

    @Test
    fun `supplier busca pelo id`() =
        runTest {
            val api = FakeSupplierApi(listOf(seara))
            val repository = NetworkSupplierRepository(api, json)

            val result = repository.supplier(1)

            assertEquals(seara, result.getOrNull())
            assertEquals(1, api.lastFindId)
        }

    @Test
    fun `falha de rede vira Result de falha`() =
        runTest {
            val api = FakeSupplierApi(emptyList(), error = IOException("sem sinal"))
            val repository = NetworkSupplierRepository(api, json)

            val result = repository.suppliers(kitchenId = 7)

            assertTrue(result.isFailure)
        }
}
