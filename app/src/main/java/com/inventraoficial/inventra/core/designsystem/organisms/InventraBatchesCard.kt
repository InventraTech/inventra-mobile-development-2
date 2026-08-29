package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSectionLabel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.core.designsystem.molecules.InventraBatchRow
import com.inventraoficial.inventra.ui.theme.InventraPurple

data class InventraBatch(
    val dateLabel: String,
    val code: String,
    val quantity: String,
    val isUrgent: Boolean = false,
)

@Composable
fun InventraBatchesCard(
    title: String,
    batches: List<InventraBatch>,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, InventraPurple.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        InventraSectionLabel(text = title)
        batches.forEach { batch ->
            InventraBatchRow(
                dateLabel = batch.dateLabel,
                code = batch.code,
                quantity = batch.quantity,
                isUrgent = batch.isUrgent,
            )
        }
        InventraTextLink(
            text = "Ver todos os lotes",
            onClick = onSeeAll,
            showArrow = true,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Preview
@Composable
private fun InventraBatchesCardPreview() {
    InventraBatchesCard(
        title = "Lotes próximos da validade",
        batches =
            listOf(
                InventraBatch("AMANHÃ", "000067", "6 L", isUrgent = true),
                InventraBatch("06/07/2026", "000069", "6 L"),
            ),
        onSeeAll = {},
    )
}
