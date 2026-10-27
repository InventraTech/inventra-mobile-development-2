package com.inventraoficial.inventra.ui.event

import androidx.compose.runtime.staticCompositionLocalOf
import com.inventraoficial.inventra.data.event.ClickTracker
import com.inventraoficial.inventra.data.event.UiClick

val LocalClickTracker = staticCompositionLocalOf<ClickTracker> { NoClickTracker }

fun ClickTracker.tracked(
    click: UiClick,
    action: () -> Unit,
): () -> Unit =
    {
        track(click)
        action()
    }

private object NoClickTracker : ClickTracker {
    override fun track(click: UiClick) = Unit

    override fun send() = Unit

    override fun sendOnLogin() = Unit
}
