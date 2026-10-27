package com.inventraoficial.inventra.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NavigatorSaverTest {
    private val scope = SaverScope { true }

    private fun navigator(
        vararg stack: Screen,
        home: Screen = Screen.Home,
    ) = Navigator(mutableStateListOf(*stack), home)

    private fun Navigator.rotate(): Navigator? {
        val saver = navigatorSaver()
        val saved = with(saver) { scope.save(this@rotate) } ?: return null
        return saver.restore(saved)
    }

    @Test
    fun `pilha de telas volta igual depois de girar a tela`() {
        val navigator = navigator(Screen.Home, Screen.StockList, Screen.StockDetail(productId = 7))

        val restored = navigator.rotate()

        assertEquals(
            listOf(Screen.Home, Screen.StockList, Screen.StockDetail(productId = 7)),
            restored?.currentBackStack,
        )
    }

    @Test
    fun `comprador continua com as requisicoes como inicio depois de girar a tela`() {
        val navigator = navigator(Screen.RequirementsList, Screen.Suppliers, home = Screen.RequirementsList)

        val restored = navigator.rotate()
        restored?.navigateHome()

        assertEquals(Screen.RequirementsList, restored?.home)
        assertEquals(listOf(Screen.RequirementsList), restored?.currentBackStack)
    }

    @Test
    fun `estado salvo ilegivel abre o app pela pilha inicial`() {
        val restored = navigatorSaver().restore("""{"backStack":[{"type":"TelaQueNaoExisteMais"}]}""")

        assertNull(restored)
    }

    @Test
    fun `pilha salva vazia abre o app pela pilha inicial`() {
        val navigator = navigator()

        val restored = navigator.rotate()

        assertNull(restored)
    }
}
