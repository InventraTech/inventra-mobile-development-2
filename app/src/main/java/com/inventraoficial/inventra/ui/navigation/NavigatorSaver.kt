package com.inventraoficial.inventra.ui.navigation

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val backStackSerializer = ListSerializer(Screen.serializer())

fun navigatorSaver(): Saver<Navigator, String> =
    Saver(
        save = { Json.encodeToString(backStackSerializer, it.currentBackStack) },
        restore = { Navigator(Json.decodeFromString(backStackSerializer, it).toMutableStateList()) },
    )
