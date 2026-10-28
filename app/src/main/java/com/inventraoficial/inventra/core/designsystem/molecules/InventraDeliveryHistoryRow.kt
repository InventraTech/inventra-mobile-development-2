package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraDeliveryHistoryRow(
    title: String,
    subtitle: String,
    quantityLabel: String,
    modifier: Modifier = Modifier,
    quantityColor: Color = InventraPurple,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(InventraPurple.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = InventraInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                fontFamily = Montserrat,
                fontSize = 12.sp,
                color = InventraMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = quantityLabel,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = quantityColor,
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraDeliveryHistoryRowPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InventraDeliveryHistoryRow(
            title = "Mauro Rosa · Compra",
            subtitle = "12/10/2026 14:30 · lote QA-01",
            quantityLabel = "+12 L",
            quantityColor = InventraSuccess,
        )
        InventraDeliveryHistoryRow(
            title = "Mauro Rosa · Uso",
            subtitle = "12/10/2026 18:05 · lote QA-01",
            quantityLabel = "−6 L",
            quantityColor = InventraDanger,
        )
    }
}
