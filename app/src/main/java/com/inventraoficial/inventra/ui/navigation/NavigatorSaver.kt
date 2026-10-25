package com.inventraoficial.inventra.ui.navigation

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class SavedNavigator(
    val backStack: List<Screen>,
    val home: Screen,
) {
    fun toNavigator() = Navigator(backStack.toMutableStateList(), home)
}

private fun Navigator.toSaved() = SavedNavigator(currentBackStack, home)

fun navigatorSaver(): Saver<Navigator, String> =
    Saver(
        save = { Json.encodeToString(SavedNavigator.serializer(), it.toSaved()) },
        restore = { saved ->
            runCatching { Json.decodeFromString(SavedNavigator.serializer(), saved) }
                .getOrNull()
                ?.toNavigator()
        },
    )
