package com.inventraoficial.inventra.data.remote.dto.event

import kotlinx.serialization.Serializable

@Serializable
data class UiClickBatchResponse(
    val recordedElements: Int = 0,
    val recordedClicks: Long = 0,
    val ignored: List<String> = emptyList(),
)
