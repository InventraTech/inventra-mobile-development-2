package com.inventraoficial.inventra.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList

class Navigator(
    private val backStack: SnapshotStateList<Screen>,
) {
    val currentBackStack: List<Screen>
        get() = backStack

    fun back() {
        backStack.removeLastOrNull()
    }

    fun navigate(screen: Screen) {
        backStack.add(screen)
    }

    fun navigateTopLevel(screen: Screen) {
        backStack.navigateTopLevel(screen)
    }

    /**
     * Esquece toda a pilha atual e deixa so [screen]. Diferente de
     * [navigateTopLevel], nao insere a Home por baixo - usado em
     * transicoes de fluxo (ex: apos cadastro) onde nem a Home ainda
     * faz sentido como destino.
     */
    fun replaceStack(screen: Screen) {
        backStack.clear()
        backStack.add(screen)
    }
}
