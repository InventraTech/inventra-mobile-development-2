package com.inventraoficial.inventra.data.event

import com.inventraoficial.inventra.data.local.SessionStorage
import com.inventraoficial.inventra.data.remote.api.ClickEventApi
import com.inventraoficial.inventra.data.remote.apiCall
import com.inventraoficial.inventra.data.remote.dto.event.RecordUiClicksRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

interface ClickTracker {
    fun track(click: UiClick)

    suspend fun flush()
}

class BufferedClickTracker(
    private val store: ClickCountStore,
    private val sessionStorage: SessionStorage,
    private val clickEventApi: ClickEventApi,
    private val json: Json,
    private val scope: CoroutineScope,
) : ClickTracker {
    private val flushMutex = Mutex()

    override fun track(click: UiClick) {
        scope.launch { store.add(click.column) }
    }

    override suspend fun flush() {
        flushMutex.withLock {
            val counts = store.counts()
            if (counts.isEmpty() || sessionStorage.token.first() == null) return
            apiCall(json) { clickEventApi.record(RecordUiClicksRequest(counts)) }
                .onSuccess { store.subtract(counts) }
        }
    }
}
