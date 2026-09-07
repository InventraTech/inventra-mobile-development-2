package com.example.inventra_mobile_2.core.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.core.designsystem.atoms.InventraStatusIcon
import com.example.inventra_mobile_2.core.designsystem.atoms.InventraStatusIconVariant
import com.example.inventra_mobile_2.ui.theme.InventraPurple
import com.example.inventra_mobile_2.ui.theme.Montserrat

@Composable
fun InventraNotificationRow(
    variant: InventraStatusIconVariant,
    title: String,
    subtitle: String,
    timestamp: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InventraStatusIcon(variant = variant)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
            Text(subtitle, fontFamily = Montserrat, color = InventraPurple.copy(alpha = 0.6f), fontSize = 13.sp)
        }
        Text(timestamp, fontFamily = Montserrat, fontSize = 12.sp, color = InventraPurple.copy(alpha = 0.5f))
    }
}

@Preview
@Composable
private fun InventraNotificationRowPreview() {
    InventraNotificationRow(
        variant = InventraStatusIconVariant.Danger,
        title = "Leite integral vence amanhã!",
        subtitle = "Lote 000067",
        timestamp = "08:30"
    )
}