package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.RequisitionApi
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionItemResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionReasonRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkRequisitionRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val requisition = FakeRequisitionRepository.requisition(12)
    private val item = FakeRequisitionRepository.item(1, "Leite Integral", 24.0, 5.49)

    private class FakeRequisitionApi(
        private val requisition: RequisitionResponse,
        private val items: List<RequisitionItemResponse>,
        private val error: Exception? = null,
    ) : RequisitionApi {
        var lastKitchenId: Int? = null
            private set
        var calls = mutableListOf<Pair<String, RequisitionReasonRequest?>>()
            private set

        override suspend fun list(kitchenId: Int): List<RequisitionResponse> {
            error?.let { throw it }
            lastKitchenId = kitchenId
            return listOf(requisition)
        }

        override suspend fun items(id: Int): List<RequisitionItemResponse> = items

        override suspend fun approve(id: Int): RequisitionResponse {
            calls += "approve" to null
            return requisition.copy(status = RequisitionStatus.APPROVED)
        }

        override suspend fun reject(
            id: Int,
            request: RequisitionReasonRequest,
        ): RequisitionResponse {
            calls += "reject" to request
            return requisition.copy(status = RequisitionStatus.REJECTED)
        }

        override suspend fun cancel(
            id: Int,
            request: RequisitionReasonRequest,
        ): RequisitionResponse {
            calls += "cancel" to request
            return requisition.copy(status = RequisitionStatus.CANCELLED)
        }
    }

    @Test
    fun `requisitions busca a cozinha inteira`() =
        runTest {
            val api = FakeRequisitionApi(requisition, listOf(item))
            val repository = NetworkRequisitionRepository(api, json)

            val result = repository.requisitions(kitchenId = 7)

            assertEquals(listOf(requisition), result.getOrNull())
            assertEquals(7, api.lastKitchenId)
        }

    @Test
    fun `items devolve os itens da requisicao`() =
        runTest {
            val repository = NetworkRequisitionRepository(FakeRequisitionApi(requisition, listOf(item)), json)

            assertEquals(listOf(item), repository.items(12).getOrNull())
        }

    @Test
    fun `reject e cancel mandam o motivo no corpo`() =
        runTest {
            val api = FakeRequisitionApi(requisition, emptyList())
            val repository = NetworkRequisitionRepository(api, json)

            repository.reject(12, "Fora do orçamento")
            repository.cancel(12, "Pedido duplicado")

            assertEquals(
                listOf(
                    "reject" to RequisitionReasonRequest("Fora do orçamento"),
                    "cancel" to RequisitionReasonRequest("Pedido duplicado"),
                ),
                api.calls,
            )
        }

    @Test
    fun `approve devolve a requisicao aprovada`() =
        runTest {
            val repository = NetworkRequisitionRepository(FakeRequisitionApi(requisition, emptyList()), json)

            assertEquals(RequisitionStatus.APPROVED, repository.approve(12).getOrNull()?.status)
        }

    @Test
    fun `falha de rede vira Result de falha`() =
        runTest {
            val api = FakeRequisitionApi(requisition, emptyList(), error = IOException("sem sinal"))
            val repository = NetworkRequisitionRepository(api, json)

            assertTrue(repository.requisitions(kitchenId = 7).isFailure)
        }
}
