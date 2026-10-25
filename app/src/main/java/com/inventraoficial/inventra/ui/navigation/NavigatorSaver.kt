package com.inventraoficial.inventra.ui.navigation

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class SavedNavigator(
    val backStack: List<Screen>,
    val home: Screen,
)

fun navigatorSaver(): Saver<Navigator, String> =
    Saver(
        save = { Json.encodeToString(SavedNavigator.serializer(), SavedNavigator(it.currentBackStack, it.home)) },
        restore = {
            val saved = Json.decodeFromString(SavedNavigator.serializer(), it)
            Navigator(saved.backStack.toMutableStateList(), saved.home)
        },
    )
