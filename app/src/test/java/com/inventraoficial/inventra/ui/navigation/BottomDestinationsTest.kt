package com.inventraoficial.inventra.ui.navigation

import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomDestination
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BottomDestinationsTest {
    @Test
    fun `estoquista nao ve a aba de requisicoes`() {
        assertFalse(InventraBottomDestination.History in bottomDestinationsFor(AccessType.ESTOQUISTA))
    }

    @Test
    fun `supervisor e comprador veem todas as abas`() {
        assertEquals(InventraBottomDestination.entries, bottomDestinationsFor(AccessType.SUPERVISOR))
        assertEquals(InventraBottomDestination.entries, bottomDestinationsFor(AccessType.COMPRADOR))
    }
}
