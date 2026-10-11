package com.inventraoficial.inventra.feature.requirements

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.repository.FakeRequisitionRepository

object RequisitionTestUsers {
    val supervisor =
        SessionUser(
            id = FakeRequisitionRepository.supervisor.id,
            name = FakeRequisitionRepository.supervisor.name,
            email = "jones@inventra.com",
            accessType = AccessType.SUPERVISOR,
            kitchenId = 7,
            kitchenName = "Sabor Filial Sul",
        )

    val buyer =
        SessionUser(
            id = FakeRequisitionRepository.buyer.id,
            name = FakeRequisitionRepository.buyer.name,
            email = "maria@inventra.com",
            accessType = AccessType.COMPRADOR,
            kitchenId = 7,
            kitchenName = "Sabor Filial Sul",
        )
}
