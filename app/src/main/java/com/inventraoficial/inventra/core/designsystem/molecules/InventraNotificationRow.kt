package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    isUnread: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (isUnread) InventraPurple.copy(alpha = 0.06f) else Color.Transparent)
                .border(1.dp, InventraPurple.copy(alpha = if (isUnread) 0.4f else 0.2f), shape)
                .then(clickModifier)
                .heightIn(min = 62.dp)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InventraStatusIcon(variant = variant)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontFamily = Montserrat, fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Bold)
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
