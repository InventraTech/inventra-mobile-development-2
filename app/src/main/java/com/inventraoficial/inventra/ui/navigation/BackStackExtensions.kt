package com.inventraoficial.inventra.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList

fun SnapshotStateList<Screen>.navigateTopLevel(screen: Screen) {
    if (lastOrNull() == screen) return
    clear()
    add(Screen.Home)
    if (screen != Screen.Home) {
        add(screen)
    }
}
