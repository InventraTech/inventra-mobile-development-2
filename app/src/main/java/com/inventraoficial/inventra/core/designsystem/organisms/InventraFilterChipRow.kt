package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraFilterChip

@Composable
fun InventraFilterChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options) { option ->
            InventraFilterChip(
                label = option,
                selected = option == selected,
                onClick = { onSelect(option) },
            )
        }
    }
}

@Preview
@Composable
private fun InventraFilterChipRowPreview() {
    InventraFilterChipRow(
        options = listOf("Todos", "Carnes", "Laticínios"),
        selected = "Todos",
        onSelect = {},
    )
}
