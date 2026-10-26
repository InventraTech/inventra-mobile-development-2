package com.inventraoficial.inventra.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigatorTest {
    private fun navigator(
        vararg stack: Screen,
        home: Screen = Screen.Home,
    ) = Navigator(mutableStateListOf(*stack), home)

    @Test
    fun `aba de baixo fica por cima da tela inicial`() {
        val navigator = navigator(Screen.Home)

        navigator.navigateTopLevel(Screen.Suppliers)

        assertEquals(listOf(Screen.Home, Screen.Suppliers), navigator.currentBackStack)
    }

    @Test
    fun `comprador tem as requisicoes como tela inicial`() {
        val navigator = navigator(Screen.RequirementsList, home = Screen.RequirementsList)

        navigator.navigateTopLevel(Screen.Suppliers)
        assertEquals(listOf(Screen.RequirementsList, Screen.Suppliers), navigator.currentBackStack)

        navigator.navigateTopLevel(Screen.RequirementsList)
        assertEquals(listOf(Screen.RequirementsList), navigator.currentBackStack)
    }

    @Test
    fun `ir para o inicio depois do login troca o login pela tela inicial`() {
        val navigator = navigator(Screen.Login, home = Screen.RequirementsList)

        navigator.navigateHome()

        assertEquals(listOf(Screen.RequirementsList), navigator.currentBackStack)
    }

    @Test
    fun `trocar a tela inicial troca a Home que ja estava na base`() {
        val navigator = navigator(Screen.Home, Screen.Notifications)

        navigator.updateHome(Screen.RequirementsList)

        assertEquals(listOf(Screen.RequirementsList, Screen.Notifications), navigator.currentBackStack)
        assertEquals(Screen.RequirementsList, navigator.home)
    }

    @Test
    fun `trocar a tela inicial nao duplica quando ela ja esta logo acima`() {
        val navigator = navigator(Screen.Home, Screen.RequirementsList)

        navigator.updateHome(Screen.RequirementsList)

        assertEquals(listOf(Screen.RequirementsList), navigator.currentBackStack)
    }

    @Test
    fun `trocar a tela inicial nao mexe na pilha que comeca em outra tela`() {
        val navigator = navigator(Screen.Login)

        navigator.updateHome(Screen.RequirementsList)

        assertEquals(listOf(Screen.Login), navigator.currentBackStack)
        assertEquals(Screen.RequirementsList, navigator.home)
    }
}
