package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraSupplierProductRow(
    name: String,
    infoText: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(28.dp)
                    .background(InventraPurple.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
        )
        Text(
            text = name,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            color = InventraPurple,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = infoText,
            fontFamily = Montserrat,
            fontSize = 11.sp,
            color = InventraPurple.copy(alpha = 0.6f),
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraSupplierProductRowPreview() {
    InventraSupplierProductRow(name = "Arroz Camil", infoText = "R$7,00/un · 3 dias")
}
