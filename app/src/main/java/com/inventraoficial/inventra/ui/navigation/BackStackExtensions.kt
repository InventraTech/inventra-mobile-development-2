package com.inventraoficial.inventra.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList

fun SnapshotStateList<Screen>.navigateTopLevel(
    screen: Screen,
    home: Screen = Screen.Home,
) {
    if (lastOrNull() == screen) return
    clear()
    add(home)
    if (screen != home) {
        add(screen)
    }
}

fun SnapshotStateList<Screen>.replaceHome(
    oldHome: Screen,
    newHome: Screen,
) {
    if (oldHome == newHome || firstOrNull() != oldHome) return
    if (getOrNull(1) == newHome) {
        removeAt(0)
    } else {
        set(0, newHome)
    }
}
