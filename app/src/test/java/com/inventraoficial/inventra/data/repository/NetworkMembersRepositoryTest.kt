package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.KitchenAccessApi
import com.inventraoficial.inventra.data.remote.api.UserApi
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestResponse
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestStatus
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.RejectKitchenAccessRequest
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkMembersRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val request = FakeMembersRepository.pendingRequest

    private class FakeKitchenAccessApi(
        private val response: KitchenAccessRequestResponse,
        private val error: Exception? = null,
    ) : KitchenAccessApi {
        var lastStatus: KitchenAccessRequestStatus? = null
            private set
        var lastApprovedId: String? = null
            private set
        var lastReject: Pair<String, RejectKitchenAccessRequest>? = null
            private set

        override suspend fun list(status: KitchenAccessRequestStatus?): List<KitchenAccessRequestResponse> {
            error?.let { throw it }
            lastStatus = status
            return listOf(response)
        }

        override suspend fun approve(id: String): KitchenAccessRequestResponse {
            lastApprovedId = id
            return response.copy(status = KitchenAccessRequestStatus.APPROVED)
        }

        override suspend fun reject(
            id: String,
            request: RejectKitchenAccessRequest,
        ): KitchenAccessRequestResponse {
            lastReject = id to request
            return response.copy(status = KitchenAccessRequestStatus.REJECTED)
        }
    }

    private class FakeUserApi : UserApi {
        override suspend fun listMembers(): List<UserResponse> = listOf(FakeMembersRepository.supervisor)

        override suspend fun me(): UserResponse = FakeMembersRepository.supervisor

        override suspend fun updatePhoto(file: MultipartBody.Part): UserResponse = error("nao usado")

        override suspend fun removePhoto(): UserResponse = error("nao usado")
    }

    @Test
    fun `pendingRequests pede so as solicitacoes pendentes`() =
        runTest {
            val api = FakeKitchenAccessApi(request)
            val repository = NetworkMembersRepository(api, FakeUserApi(), json)

            val result = repository.pendingRequests()

            assertEquals(listOf(request), result.getOrNull())
            assertEquals(KitchenAccessRequestStatus.PENDING, api.lastStatus)
        }

    @Test
    fun `members lista os usuarios da cozinha`() =
        runTest {
            val repository = NetworkMembersRepository(FakeKitchenAccessApi(request), FakeUserApi(), json)

            val result = repository.members()

            assertEquals(listOf(FakeMembersRepository.supervisor), result.getOrNull())
        }

    @Test
    fun `approve envia o id da solicitacao`() =
        runTest {
            val api = FakeKitchenAccessApi(request)
            val repository = NetworkMembersRepository(api, FakeUserApi(), json)

            val result = repository.approve("req-1")

            assertEquals("req-1", api.lastApprovedId)
            assertEquals(KitchenAccessRequestStatus.APPROVED, result.getOrNull()?.status)
        }

    @Test
    fun `reject envia o id e o motivo`() =
        runTest {
            val api = FakeKitchenAccessApi(request)
            val repository = NetworkMembersRepository(api, FakeUserApi(), json)

            repository.reject("req-1", "Não faz parte da equipe")

            assertEquals("req-1" to RejectKitchenAccessRequest("Não faz parte da equipe"), api.lastReject)
        }

    @Test
    fun `falha de rede vira Result de falha`() =
        runTest {
            val api = FakeKitchenAccessApi(request, error = IOException("sem sinal"))
            val repository = NetworkMembersRepository(api, FakeUserApi(), json)

            val result = repository.pendingRequests()

            assertTrue(result.isFailure)
        }
}
