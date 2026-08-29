package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraStatusIcon
import com.inventraoficial.inventra.core.designsystem.atoms.InventraStatusIconVariant
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraNotificationRow(
    variant: InventraStatusIconVariant,
    title: String,
    subtitle: String,
    timestamp: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .heightIn(min = 62.dp)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
        timestamp = "08:30",
    )
}
