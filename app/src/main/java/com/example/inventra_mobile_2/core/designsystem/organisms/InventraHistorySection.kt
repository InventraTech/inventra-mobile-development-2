package com.example.inventra_mobile_2.core.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.inventra_mobile_2.core.designsystem.atoms.InventraSecondaryButton
import com.example.inventra_mobile_2.core.designsystem.atoms.InventraSectionLabel

@Composable
fun InventraHistorySection(
    onEntriesClick: () -> Unit,
    onExitsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InventraSectionLabel(text = "Histórico")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InventraSecondaryButton(text = "Entradas", onClick = onEntriesClick, modifier = Modifier.weight(1f))
            InventraSecondaryButton(text = "Saídas", onClick = onExitsClick, modifier = Modifier.weight(1f))
        }
    }
}

@Preview
@Composable
private fun InventraHistorySectionPreview() {
    InventraHistorySection(onEntriesClick = {}, onExitsClick = {})
}