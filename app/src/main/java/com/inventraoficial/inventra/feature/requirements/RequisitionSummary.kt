package com.inventraoficial.inventra.feature.requirements

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

data class RequisitionSummary(
    val id: Int,
    val title: String,
    val subtitle: String,
    val details: String,
    val status: RequisitionStatus,
)

val RequisitionType.label: String
    get() =
        when (this) {
            RequisitionType.PURCHASE -> "Compra"
            RequisitionType.TRANSFER -> "Transferência"
            RequisitionType.CONSUMPTION -> "Consumo"
        }

val RequisitionStatus.label: String
    get() =
        when (this) {
            RequisitionStatus.UNDER_REVIEW -> "Em análise"
            RequisitionStatus.APPROVED -> "Aprovada"
            RequisitionStatus.REJECTED -> "Recusada"
            RequisitionStatus.CANCELLED -> "Cancelada"
        }

private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

internal fun RequisitionResponse.createdDateText(zone: ZoneId): String =
    LocalDateTime
        .parse(createdAt)
        .atZone(ZoneOffset.UTC)
        .withZoneSameInstant(zone)
        .format(dateFormatter)

internal fun RequisitionResponse.titleText(): String = "${type.label} #$id"

internal fun RequisitionResponse.subtitleText(zone: ZoneId): String {
    val requesterName = requester?.name ?: "Solicitante desconhecido"
    return "$requesterName · ${createdDateText(zone)}"
}

internal fun RequisitionResponse.isVisibleTo(user: SessionUser): Boolean =
    user.accessType == AccessType.SUPERVISOR || requester?.id == user.id

internal fun List<RequisitionResponse>.newestFirst() = sortedByDescending { LocalDateTime.parse(it.createdAt) }

internal fun List<String>.itemsSummary(): String =
    when (size) {
        0 -> "Sem itens"
        1, 2 -> joinToString(", ")
        else -> "${take(2).joinToString(", ")} e mais ${size - 2}"
    }

internal fun Double.quantityText(): String = if (this % 1.0 == 0.0) toLong().toString() else toString()

internal fun Double.currencyText(): String = String.format(ptBr, "R$ %,.2f", this)
