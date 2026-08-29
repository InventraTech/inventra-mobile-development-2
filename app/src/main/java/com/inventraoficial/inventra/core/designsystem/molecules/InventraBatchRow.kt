package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBadge
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBadgeVariant
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraBatchRow(
    dateLabel: String,
    code: String,
    quantity: String,
    modifier: Modifier = Modifier,
    isUrgent: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isUrgent) {
            InventraBadge(text = dateLabel, variant = InventraBadgeVariant.DateAlert)
        } else {
            Text(dateLabel, fontFamily = Montserrat, color = InventraPurple.copy(alpha = 0.6f), fontSize = 12.sp)
        }
        Text(code, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
        Text(quantity, fontFamily = Montserrat)
    }
}

@Preview
@Composable
private fun InventraBatchRowPreview() {
    InventraBatchRow(dateLabel = "AMANHÃ", code = "000067", quantity = "6 L", isUrgent = true)
}
