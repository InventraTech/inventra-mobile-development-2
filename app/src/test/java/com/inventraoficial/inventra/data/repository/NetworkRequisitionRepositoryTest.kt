package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.RequisitionApi
import com.inventraoficial.inventra.data.remote.dto.requisition.AddRequisitionItemRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.CreateRequisitionRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionItemResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionReasonRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType
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
        var lastList: Triple<Int, RequisitionStatus?, String?>? = null
            private set
        var calls = mutableListOf<Pair<String, Any?>>()
            private set

        override suspend fun list(
            kitchenId: Int,
            status: RequisitionStatus?,
            requesterId: String?,
        ): List<RequisitionResponse> {
            error?.let { throw it }
            lastList = Triple(kitchenId, status, requesterId)
            return listOf(requisition)
        }

        override suspend fun find(id: Int): RequisitionResponse {
            calls += "find" to id
            return requisition
        }

        override suspend fun items(id: Int): List<RequisitionItemResponse> = items

        override suspend fun create(request: CreateRequisitionRequest): RequisitionResponse {
            calls += "create" to request
            return requisition.copy(status = RequisitionStatus.DRAFT)
        }

        override suspend fun addItem(
            id: Int,
            request: AddRequisitionItemRequest,
        ): RequisitionResponse {
            calls += "addItem" to request
            return requisition
        }

        override suspend fun removeItem(
            id: Int,
            itemId: Int,
        ): RequisitionResponse {
            calls += "removeItem" to itemId
            return requisition
        }

        override suspend fun submit(id: Int): RequisitionResponse {
            calls += "submit" to id
            return requisition.copy(status = RequisitionStatus.UNDER_REVIEW)
        }

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
    fun `requisitions envia os filtros combinados`() =
        runTest {
            val api = FakeRequisitionApi(requisition, listOf(item))
            val repository = NetworkRequisitionRepository(api, json)

            val result =
                repository.requisitions(kitchenId = 7, status = RequisitionStatus.UNDER_REVIEW, requesterId = "buy-1")

            assertEquals(listOf(requisition), result.getOrNull())
            assertEquals(Triple(7, RequisitionStatus.UNDER_REVIEW, "buy-1"), api.lastList)
        }

    @Test
    fun `requisition busca pelo id`() =
        runTest {
            val api = FakeRequisitionApi(requisition, emptyList())
            val repository = NetworkRequisitionRepository(api, json)

            assertEquals(requisition, repository.requisition(12).getOrNull())
            assertEquals(listOf("find" to 12), api.calls)
        }

    @Test
    fun `items devolve os itens da requisicao`() =
        runTest {
            val repository = NetworkRequisitionRepository(FakeRequisitionApi(requisition, listOf(item)), json)

            assertEquals(listOf(item), repository.items(12).getOrNull())
        }

    @Test
    fun `rascunho cria adiciona remove e envia`() =
        runTest {
            val api = FakeRequisitionApi(requisition, emptyList())
            val repository = NetworkRequisitionRepository(api, json)
            val create = CreateRequisitionRequest(RequisitionType.PURCHASE, "Reposição", 7)
            val add = AddRequisitionItemRequest(productId = 5013, quantity = 20.0, estimatedPrice = 4.5)

            assertEquals(RequisitionStatus.DRAFT, repository.create(create).getOrNull()?.status)
            repository.addItem(12, add)
            repository.removeItem(12, 3)
            assertEquals(RequisitionStatus.UNDER_REVIEW, repository.submit(12).getOrNull()?.status)

            assertEquals(
                listOf("create" to create, "addItem" to add, "removeItem" to 3, "submit" to 12),
                api.calls,
            )
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
