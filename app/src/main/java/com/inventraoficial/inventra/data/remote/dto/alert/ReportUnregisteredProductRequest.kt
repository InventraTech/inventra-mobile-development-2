package com.inventraoficial.inventra.data.remote.dto.alert

import kotlinx.serialization.Serializable

@Serializable
data class ReportUnregisteredProductRequest(
    val kitchenId: Int,
    val barcode: String,
)
