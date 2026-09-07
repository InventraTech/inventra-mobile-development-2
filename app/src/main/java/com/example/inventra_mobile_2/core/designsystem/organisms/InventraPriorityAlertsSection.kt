package com.example.inventra_mobile_2.core.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.inventra_mobile_2.R
import com.example.inventra_mobile_2.core.designsystem.molecules.InventraPriorityRow
import com.example.inventra_mobile_2.core.designsystem.molecules.InventraUrgency
import com.example.inventra_mobile_2.ui.theme.InventraDanger
import com.example.inventra_mobile_2.ui.theme.Montserrat

data class InventraPriorityItem(
    val painter: Painter,
    val title: String,
    val statusText: String,
    val urgency: InventraUrgency
)

@Composable
fun InventraPriorityAlertsSection(
    items: List<InventraPriorityItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("⚠ Produtos com prioridade", color = InventraDanger, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
        items.forEach { item ->
            InventraPriorityRow(
                painter = item.painter,
                title = item.title,
                statusText = item.statusText,
                urgency = item.urgency
            )
        }
    }
}

@Preview
@Composable
private fun InventraPriorityAlertsSectionPreview() {
    InventraPriorityAlertsSection(
        items = listOf(
            InventraPriorityItem(painterResource(R.drawable.ic_inventra_logo), "Arroz Camil", "Vence em 2 dias", InventraUrgency.Normal)
        )
    )
}