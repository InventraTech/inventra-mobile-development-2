package com.inventraoficial.inventra.data.remote.dto.alert

import com.inventraoficial.inventra.data.remote.dto.stock.ProductSummary
import kotlinx.serialization.Serializable

@Serializable
data class AlertResponse(
    val id: Int,
    val type: String,
    val severity: AlertSeverity,
    val batch: BatchSummary? = null,
    val product: ProductSummary? = null,
    val message: String,
    val read: Boolean = false,
    val refKey: String? = null,
    val createdAt: String,
)

@Serializable
data class BatchSummary(
    val id: Int,
    val batchNumber: String,
)

@Serializable
enum class AlertSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
}
