package com.inventraoficial.inventra.feature.requirements

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
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
            RequisitionStatus.DRAFT -> "Rascunho"
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

internal fun RequisitionResponse.itemsDetails(): String {
    if (itemCount == 0) return "Sem itens"
    val shown = itemNames.take(NAMES_IN_CARD)
    val rest = itemCount - shown.size
    val names = if (rest > 0) "${shown.joinToString(", ")} e mais $rest" else shown.joinToString(", ")
    return if (estimatedTotal > 0) "$names · ${estimatedTotal.currencyText()}" else names
}

private const val NAMES_IN_CARD = 2

internal fun Double.quantityText(): String = DecimalFormat("#,##0.###", DecimalFormatSymbols(ptBr)).format(this)

internal fun Double.currencyText(): String = String.format(ptBr, "R$ %,.2f", this)
