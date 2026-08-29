package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraStatusIconVariant
import com.inventraoficial.inventra.core.designsystem.molecules.InventraNotificationRow
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

data class InventraNotification(
    val variant: InventraStatusIconVariant,
    val title: String,
    val subtitle: String,
    val timestamp: String,
)

@Composable
fun InventraGroupedNotifications(
    groups: Map<String, List<InventraNotification>>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        groups.forEach { (dateHeader, notifications) ->
            item {
                Text(dateHeader, color = InventraPurple, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            items(notifications) { notification ->
                InventraNotificationRow(
                    variant = notification.variant,
                    title = notification.title,
                    subtitle = notification.subtitle,
                    timestamp = notification.timestamp,
                )
            }
        }
    }
}

@Preview
@Composable
private fun InventraGroupedNotificationsPreview() {
    InventraGroupedNotifications(
        groups =
            mapOf(
                "Hoje" to
                    listOf(InventraNotification(InventraStatusIconVariant.Danger, "Leite integral vence amanhã!", "Lote 000067", "08:30")),
            ),
    )
}
