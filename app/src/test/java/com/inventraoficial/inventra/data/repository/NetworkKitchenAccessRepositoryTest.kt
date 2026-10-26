package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.local.GeoPoint
import com.inventraoficial.inventra.data.remote.api.KitchenAccessApi
import com.inventraoficial.inventra.data.remote.api.KitchenApi
import com.inventraoficial.inventra.data.remote.dto.kitchen.CreateKitchenRequest
import com.inventraoficial.inventra.data.remote.dto.kitchen.DiscoverKitchenPage
import com.inventraoficial.inventra.data.remote.dto.kitchen.KitchenResponse
import com.inventraoficial.inventra.data.remote.dto.kitchen.UpdateKitchenRequest
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.CreateKitchenAccessRequest
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestResponse
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestStatus
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.RejectKitchenAccessRequest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class NetworkKitchenAccessRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val request = FakeMembersRepository.pendingRequest

    private class FakeKitchenApi : KitchenApi {
        var lastDiscover: List<Any?>? = null
            private set

        override suspend fun create(request: CreateKitchenRequest): KitchenResponse = error("nao usado")

        override suspend fun discover(
            query: String?,
            latitude: Double?,
            longitude: Double?,
            size: Int,
        ): DiscoverKitchenPage {
            lastDiscover = listOf(query, latitude, longitude, size)
            return DiscoverKitchenPage(content = listOf(FakeKitchenAccessRepository.perto))
        }

        override suspend fun find(id: Int): KitchenResponse = error("nao usado")

        override suspend fun update(
            id: Int,
            request: UpdateKitchenRequest,
        ): KitchenResponse = error("nao usado")
    }

    private class FakeKitchenAccessApi(
        private val response: KitchenAccessRequestResponse,
        private val mineError: Exception? = null,
    ) : KitchenAccessApi {
        var lastCreate: CreateKitchenAccessRequest? = null
            private set

        override suspend fun list(status: KitchenAccessRequestStatus?): List<KitchenAccessRequestResponse> {
            error("nao usado")
        }

        override suspend fun approve(id: String): KitchenAccessRequestResponse = error("nao usado")

        override suspend fun reject(
            id: String,
            request: RejectKitchenAccessRequest,
        ): KitchenAccessRequestResponse = error("nao usado")

        override suspend fun create(request: CreateKitchenAccessRequest): KitchenAccessRequestResponse {
            lastCreate = request
            return response
        }

        override suspend fun mine(): KitchenAccessRequestResponse {
            mineError?.let { throw it }
            return response
        }
    }

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))

    @Test
    fun `discover manda a busca e a localizacao e devolve o conteudo da pagina`() =
        runTest {
            val kitchenApi = FakeKitchenApi()
            val repository = NetworkKitchenAccessRepository(kitchenApi, FakeKitchenAccessApi(request), json)

            val result = repository.discover("  sabor  ", GeoPoint(-23.5, -46.6))

            assertEquals(listOf(FakeKitchenAccessRepository.perto), result.getOrNull())
            assertEquals(listOf("sabor", -23.5, -46.6, 50), kitchenApi.lastDiscover)
        }

    @Test
    fun `discover sem busca e sem localizacao manda tudo nulo`() =
        runTest {
            val kitchenApi = FakeKitchenApi()
            val repository = NetworkKitchenAccessRepository(kitchenApi, FakeKitchenAccessApi(request), json)

            repository.discover("   ")

            assertEquals(listOf(null, null, null, 50), kitchenApi.lastDiscover)
        }

    @Test
    fun `requestAccess cria a solicitacao para a cozinha escolhida`() =
        runTest {
            val accessApi = FakeKitchenAccessApi(request)
            val repository = NetworkKitchenAccessRepository(FakeKitchenApi(), accessApi, json)

            val result = repository.requestAccess(7)

            assertEquals(request, result.getOrNull())
            assertEquals(CreateKitchenAccessRequest(kitchenId = 7), accessApi.lastCreate)
        }

    @Test
    fun `myRequest devolve a solicitacao do usuario`() =
        runTest {
            val repository = NetworkKitchenAccessRepository(FakeKitchenApi(), FakeKitchenAccessApi(request), json)

            assertEquals(request, repository.myRequest().getOrNull())
        }

    @Test
    fun `myRequest com 404 significa que ainda nao ha solicitacao`() =
        runTest {
            val api = FakeKitchenAccessApi(request, mineError = httpError(404))
            val repository = NetworkKitchenAccessRepository(FakeKitchenApi(), api, json)

            val result = repository.myRequest()

            assertTrue(result.isSuccess)
            assertNull(result.getOrNull())
        }

    @Test
    fun `myRequest com outro erro devolve falha`() =
        runTest {
            val api = FakeKitchenAccessApi(request, mineError = httpError(500))
            val repository = NetworkKitchenAccessRepository(FakeKitchenApi(), api, json)

            assertTrue(repository.myRequest().isFailure)
        }

    @Test
    fun `myRequest sem conexao devolve falha`() =
        runTest {
            val api = FakeKitchenAccessApi(request, mineError = IOException("sem sinal"))
            val repository = NetworkKitchenAccessRepository(FakeKitchenApi(), api, json)

            assertTrue(repository.myRequest().isFailure)
        }
}
