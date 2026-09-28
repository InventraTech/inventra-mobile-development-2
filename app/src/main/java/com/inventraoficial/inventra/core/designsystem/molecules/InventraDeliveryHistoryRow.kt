package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraDeliveryHistoryRow(
    date: String,
    productName: String,
    quantityLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(InventraPurple.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = date, fontFamily = Montserrat, fontSize = 13.sp, color = Color.Gray)
        Text(text = productName, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = quantityLabel, fontFamily = Montserrat, fontSize = 13.sp, color = Color.Gray)
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraDeliveryHistoryRowPreview() {
    InventraDeliveryHistoryRow(date = "14/08/2026", productName = "Arroz Camil", quantityLabel = "12 un")
}
