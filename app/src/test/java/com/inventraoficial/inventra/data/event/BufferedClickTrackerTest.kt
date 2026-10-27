package com.inventraoficial.inventra.data.event

import com.inventraoficial.inventra.data.local.FakeSessionStorage
import com.inventraoficial.inventra.data.remote.api.ClickEventApi
import com.inventraoficial.inventra.data.remote.dto.event.RecordUiClicksRequest
import com.inventraoficial.inventra.data.remote.dto.event.UiClickBatchResponse
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class BufferedClickTrackerTest {
    private val json = Json { ignoreUnknownKeys = true }

    private class FakeClickEventApi(
        private val error: Exception? = null,
        private val ignored: List<String> = emptyList(),
        private val onRecord: suspend () -> Unit = {},
    ) : ClickEventApi {
        val requests = mutableListOf<RecordUiClicksRequest>()

        override suspend fun record(request: RecordUiClicksRequest): UiClickBatchResponse {
            onRecord()
            error?.let { throw it }
            requests += request
            return UiClickBatchResponse(
                recordedElements = request.clicks.size - ignored.size,
                recordedClicks = request.clicks.values.sum(),
                ignored = ignored,
            )
        }
    }

    private fun TestScope.tracker(
        store: ClickCountStore,
        api: ClickEventApi = FakeClickEventApi(),
        token: String? = "jwt",
    ) = BufferedClickTracker(
        store = store,
        sessionStorage = FakeSessionStorage(initialToken = token),
        clickEventApi = api,
        json = json,
        scope = this,
    )

    @Test
    fun `track soma cada clique no buffer`() =
        runTest {
            val store = FakeClickCountStore()
            val tracker = tracker(store)

            tracker.track(UiClick.NavHome)
            tracker.track(UiClick.NavHome)
            tracker.track(UiClick.NavStock)
            advanceUntilIdle()

            assertEquals(mapOf("nav_mb_global_home" to 2L, "nav_mb_global_stock" to 1L), store.counts())
        }

    @Test
    fun `flush envia o lote e esvazia o buffer`() =
        runTest {
            val store = FakeClickCountStore(mapOf("nav_mb_global_home" to 2L))
            val api = FakeClickEventApi()

            tracker(store, api).flush()

            assertEquals(listOf(RecordUiClicksRequest(mapOf("nav_mb_global_home" to 2L))), api.requests)
            assertTrue(store.counts().isEmpty())
        }

    @Test
    fun `sem sessao nao envia e guarda os cliques para depois do login`() =
        runTest {
            val store = FakeClickCountStore(mapOf("nav_mb_global_home" to 1L))
            val api = FakeClickEventApi()

            tracker(store, api, token = null).flush()

            assertTrue(api.requests.isEmpty())
            assertEquals(mapOf("nav_mb_global_home" to 1L), store.counts())
        }

    @Test
    fun `buffer vazio nao chama a API`() =
        runTest {
            val api = FakeClickEventApi()

            tracker(FakeClickCountStore(), api).flush()

            assertTrue(api.requests.isEmpty())
        }

    @Test
    fun `falha de rede mantem os cliques no buffer`() =
        runTest {
            val store = FakeClickCountStore(mapOf("nav_mb_global_home" to 3L))
            val api = FakeClickEventApi(error = IOException("sem sinal"))

            tracker(store, api).flush()

            assertEquals(mapOf("nav_mb_global_home" to 3L), store.counts())
        }

    @Test
    fun `clique feito durante o envio fica para o proximo lote`() =
        runTest {
            val store = FakeClickCountStore(mapOf("nav_mb_global_home" to 2L))
            val api = FakeClickEventApi(onRecord = { store.add("nav_mb_global_home") })

            tracker(store, api).flush()

            assertEquals(mapOf("nav_mb_global_home" to 1L), store.counts())
        }

    @Test
    fun `elemento ignorado pelo back sai do buffer`() =
        runTest {
            val store = FakeClickCountStore(mapOf("nav_mb_global_home" to 1L, "bt_mb_inexistente" to 4L))
            val api = FakeClickEventApi(ignored = listOf("bt_mb_inexistente"))

            tracker(store, api).flush()

            assertTrue(store.counts().isEmpty())
        }
}
