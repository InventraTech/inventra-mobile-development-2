package com.inventraoficial.inventra.feature.profile

import com.inventraoficial.inventra.data.remote.dto.auth.AccessType

fun AccessType.toLabel(): String =
    when (this) {
        AccessType.SUPERVISOR -> "Supervisor"
        AccessType.ESTOQUISTA -> "Estoquista"
        AccessType.COMPRADOR -> "Comprador"
    }
