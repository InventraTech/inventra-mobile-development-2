package com.inventraoficial.inventra.data.remote.dto.event

import kotlinx.serialization.Serializable

@Serializable
data class RecordUiClicksRequest(
    val clicks: Map<String, Long>,
)
