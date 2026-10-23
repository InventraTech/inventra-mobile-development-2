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
    fun `supervisor ve todas as abas`() {
        assertEquals(InventraBottomDestination.entries, bottomDestinationsFor(AccessType.SUPERVISOR))
    }

    @Test
    fun `comprador ve so fornecedores, requisicoes e perfil`() {
        assertEquals(
            listOf(
                InventraBottomDestination.Suppliers,
                InventraBottomDestination.History,
                InventraBottomDestination.Perfil,
            ),
            bottomDestinationsFor(AccessType.COMPRADOR),
        )
    }

    @Test
    fun `comprador abre nas requisicoes e os outros na Home`() {
        assertEquals(Screen.RequirementsList, homeScreenFor(AccessType.COMPRADOR))
        assertEquals(Screen.Home, homeScreenFor(AccessType.SUPERVISOR))
        assertEquals(Screen.Home, homeScreenFor(AccessType.ESTOQUISTA))
        assertEquals(Screen.Home, homeScreenFor(null))
    }
}
