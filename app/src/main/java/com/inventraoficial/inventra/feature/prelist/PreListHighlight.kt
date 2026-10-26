package com.inventraoficial.inventra.feature.prelist

import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.prelist.PreListResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.AddRequisitionItemRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.CreateRequisitionRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType
import com.inventraoficial.inventra.data.repository.RequisitionRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class PreListHighlight(
    val id: Int,
    val dateText: String,
    val summaryText: String,
    val photoUrls: List<String?>,
    val extraCount: Int,
)

private const val MAX_PHOTOS = 4
private const val ISO_DATE_LENGTH = 10
private val shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM")

internal fun AccessType.seesPreLists(): Boolean = this in setOf(AccessType.SUPERVISOR, AccessType.COMPRADOR)

internal fun PreListResponse.shortDateText(): String {
    val date = LocalDate.parse(listDate.take(ISO_DATE_LENGTH))
    return date.format(shortDateFormatter)
}

internal fun PreListResponse.summaryText(): String {
    val suppliers = items.mapNotNull { it.supplier?.id }.distinct().size
    val itemsText = if (items.size == 1) "1 item" else "${items.size} itens"
    val suppliersText = if (suppliers == 1) "1 fornecedor" else "$suppliers fornecedores"
    return "$itemsText · $suppliersText"
}

internal fun PreListResponse.toHighlight(): PreListHighlight =
    PreListHighlight(
        id = id,
        dateText = shortDateText(),
        summaryText = summaryText(),
        photoUrls = items.sortedBy { it.product.photoUrl == null }.take(MAX_PHOTOS).map { it.product.photoUrl },
        extraCount = (items.size - MAX_PHOTOS).coerceAtLeast(0),
    )

internal suspend fun RequisitionRepository.createPurchaseFrom(
    preList: PreListResponse,
    kitchenId: Int,
): Int {
    val request = CreateRequisitionRequest(RequisitionType.PURCHASE, "Pré-lista ${preList.shortDateText()}", kitchenId)
    val draft = create(request).getOrThrow()
    preList.items.forEach { item ->
        addItem(draft.id, AddRequisitionItemRequest(item.product.id, item.quantity, item.supplier?.id)).getOrThrow()
    }
    return draft.id
}
