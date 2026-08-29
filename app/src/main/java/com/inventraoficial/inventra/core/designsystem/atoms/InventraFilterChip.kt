package com.inventraoficial.inventra.core.designsystem.atoms

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium,
            )
        },
        modifier = modifier,
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = InventraPurple,
                selectedLabelColor = Color.White,
                labelColor = InventraPurple,
            ),
    )
}

@Preview
@Composable
private fun InventraFilterChipPreview() {
    InventraFilterChip(label = "Todos", selected = false, onClick = {})
}
