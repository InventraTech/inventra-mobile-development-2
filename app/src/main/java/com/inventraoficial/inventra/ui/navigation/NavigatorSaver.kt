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

private fun Navigator.encode(): String = Json.encodeToString(SavedNavigator.serializer(), toSaved())

private fun decodeNavigator(saved: String): Navigator? =
    runCatching { Json.decodeFromString(SavedNavigator.serializer(), saved) }
        .getOrNull()
        ?.takeIf { it.backStack.isNotEmpty() }
        ?.toNavigator()

fun navigatorSaver(): Saver<Navigator, String> =
    Saver(
        save = { it.encode() },
        restore = ::decodeNavigator,
    )
