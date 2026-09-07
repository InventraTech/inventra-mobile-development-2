package com.example.inventra_mobile_2.core.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.inventra_mobile_2.core.designsystem.molecules.InventraStatTile
import com.example.inventra_mobile_2.core.designsystem.molecules.InventraStatTone

data class InventraStat(
    val label: String,
    val value: String,
    val trend: String,
    val tone: InventraStatTone
)

@Composable
fun InventraStatGrid(
    stats: List<InventraStat>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        stats.chunked(2).forEach { rowStats ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowStats.forEach { stat ->
                    InventraStatTile(
                        label = stat.label,
                        value = stat.value,
                        trend = stat.trend,
                        tone = stat.tone,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun InventraStatGridPreview() {
    InventraStatGrid(
        stats = listOf(
            InventraStat("Economia", "R$ 6767,00", "↑ 67%", InventraStatTone.Positive),
            InventraStat("Perdas", "R$ 6767,00", "↓ 67%", InventraStatTone.Negative)
        )
    )
}