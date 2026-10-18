package com.inventraoficial.inventra.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList

class Navigator(
    private val backStack: SnapshotStateList<Screen>,
) {
    val currentBackStack: List<Screen>
        get() = backStack

    val canGoBack: Boolean
        get() = backStack.size > 1

    fun back() {
        backStack.removeLastOrNull()
    }

    fun navigate(screen: Screen) {
        backStack.add(screen)
    }

    fun navigateTopLevel(screen: Screen) {
        backStack.navigateTopLevel(screen)
    }

    fun replaceStack(screen: Screen) {
        backStack.clear()
        backStack.add(screen)
    }
}
