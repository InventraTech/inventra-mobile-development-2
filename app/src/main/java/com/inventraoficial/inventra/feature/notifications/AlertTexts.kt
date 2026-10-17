package com.inventraoficial.inventra.feature.notifications

import com.inventraoficial.inventra.data.remote.dto.alert.AlertResponse

// Textos dos alertas, usados na tela de Notificações e nas notificações do celular.
internal const val EXPIRATION_TYPE = "EXPIRATION"
internal const val EXPIRING_TYPE = "EXPIRING"
internal const val STOCK_TYPE = "STOCK"
internal const val UNREGISTERED_PRODUCT_TYPE = "UNREGISTERED_PRODUCT"
internal const val ACCESS_REQUEST_TYPE = "ACCESS_REQUEST"
internal const val REQUISITION_SUBMITTED_TYPE = "REQUISITION_SUBMITTED"
internal const val REQUISITION_APPROVED_TYPE = "REQUISITION_APPROVED"
internal const val REQUISITION_REJECTED_TYPE = "REQUISITION_REJECTED"
internal const val REQUISITION_PREFIX = "REQUISITION_"

internal val stockTypes = setOf(EXPIRATION_TYPE, EXPIRING_TYPE, STOCK_TYPE, UNREGISTERED_PRODUCT_TYPE)

internal fun AlertResponse.title(): String {
    val productName = product?.name
    return when (type) {
        EXPIRATION_TYPE -> productName?.let { "$it venceu" }
        EXPIRING_TYPE -> productName?.let { "$it vence em breve" }
        STOCK_TYPE -> productName?.let { "$it abaixo do mínimo" }
        UNREGISTERED_PRODUCT_TYPE -> "Produto não cadastrado"
        ACCESS_REQUEST_TYPE -> "Pedido de entrada na cozinha"
        REQUISITION_SUBMITTED_TYPE -> "Nova requisição para análise"
        REQUISITION_APPROVED_TYPE -> "Requisição aprovada"
        REQUISITION_REJECTED_TYPE -> "Requisição recusada"
        else -> null
    } ?: message
}

internal fun AlertResponse.subtitle(): String {
    val usesMessage = title() != message
    return when (type) {
        EXPIRATION_TYPE -> batch?.let { "Lote ${it.batchNumber}" } ?: message
        STOCK_TYPE -> if (product != null) "Reponha o estoque" else ""
        else -> if (usesMessage) message else ""
    }
}
