package com.inventraoficial.inventra.feature.suppliers.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate

data class PickedSupplier(
    val id: Int,
    val name: String,
)

class SupplierSelection {
    private val _picked = MutableStateFlow<PickedSupplier?>(null)
    val picked: StateFlow<PickedSupplier?> = _picked.asStateFlow()

    fun pick(supplier: PickedSupplier) {
        _picked.value = supplier
    }

    fun consume(): PickedSupplier? = _picked.getAndUpdate { null }
}

val LocalSupplierSelection = staticCompositionLocalOf { SupplierSelection() }

@Composable
fun PickedSupplierEffect(onPicked: (PickedSupplier) -> Unit) {
    val selection = LocalSupplierSelection.current
    val picked by selection.picked.collectAsState()
    LaunchedEffect(picked) {
        selection.consume()?.let(onPicked)
    }
}
