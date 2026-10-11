package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.dto.alert.AlertResponse
import com.inventraoficial.inventra.data.remote.dto.alert.AlertSeverity
import com.inventraoficial.inventra.data.remote.dto.alert.BatchSummary
import com.inventraoficial.inventra.data.remote.dto.stock.ProductSummary

class FakeAlertRepository(
    var alerts: List<AlertResponse> = emptyList(),
) : AlertRepository {
    var error: Exception? = null
    var markAsReadError: Exception? = null
    var alertsCalls = mutableListOf<Int>()
        private set
    var markedAsRead = mutableListOf<Int>()
        private set

    override suspend fun alerts(kitchenId: Int): Result<List<AlertResponse>> {
        alertsCalls += kitchenId
        error?.let { return Result.failure(it) }
        return Result.success(alerts)
    }

    override suspend fun unreadCount(kitchenId: Int): Result<Int> {
        error?.let { return Result.failure(it) }
        return Result.success(alerts.count { !it.read })
    }

    override suspend fun markAsRead(id: Int): Result<AlertResponse> {
        markedAsRead += id
        markAsReadError?.let { return Result.failure(it) }
        val alert = alerts.first { it.id == id }.copy(read = true)
        alerts = alerts.map { if (it.id == id) alert else it }
        return Result.success(alert)
    }

    companion object {
        fun alert(
            id: Int,
            type: String,
            createdAt: String,
            productName: String? = null,
            read: Boolean = false,
        ) = AlertResponse(
            id = id,
            type = type,
            severity = if (type == "EXPIRATION") AlertSeverity.CRITICAL else AlertSeverity.MEDIUM,
            batch = BatchSummary(id = id, batchNumber = "QA-$id"),
            product = productName?.let { ProductSummary(id = id, name = it) },
            message = "Mensagem do alerta $id",
            read = read,
            createdAt = createdAt,
        )
    }
}
