package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.molecules.InventraPriorityRow
import com.inventraoficial.inventra.core.designsystem.molecules.InventraUrgency
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.Montserrat

data class InventraPriorityItem(
    val imageRes: Int,
    val title: String,
    val statusText: String,
    val urgency: InventraUrgency,
    val batch: String,
)

@Composable
fun InventraPriorityAlertsSection(
    items: List<InventraPriorityItem>,
    modifier: Modifier = Modifier,
    onItemClick: (InventraPriorityItem) -> Unit = {},
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("⚠ Produtos com prioridade", color = InventraDanger, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
        items.forEach { item ->
            InventraPriorityRow(
                painter = painterResource(item.imageRes),
                title = item.title,
                statusText = item.statusText,
                urgency = item.urgency,
                batch = item.batch,
                onClick = { onItemClick(item) },
            )
        }
    }
}

@Preview
@Composable
private fun InventraPriorityAlertsSectionPreview() {
    InventraPriorityAlertsSection(
        items =
            listOf(
                InventraPriorityItem(R.drawable.ic_inventra_logo, "Arroz Camil", "Vence em 2 dias", InventraUrgency.Normal, "Lote 0001"),
            ),
    )
}
