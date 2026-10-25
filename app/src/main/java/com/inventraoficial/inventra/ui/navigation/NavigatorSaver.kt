package com.inventraoficial.inventra.ui.navigation

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.json.Json

fun navigatorSaver(): Saver<Navigator, String> =
    Saver(
        save = { Json.encodeToString<List<Screen>>(it.currentBackStack) },
        restore = { Navigator(Json.decodeFromString<List<Screen>>(it).toMutableStateList()) },
    )
