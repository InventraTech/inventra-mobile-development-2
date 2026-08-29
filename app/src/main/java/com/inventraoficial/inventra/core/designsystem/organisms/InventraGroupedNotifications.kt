package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import java.time.LocalDate

data class InventraNotification(
    val variant: InventraStatusIconVariant,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val date: LocalDate,
)

@Composable
fun InventraGroupedNotifications(
    groups: Map<String, List<InventraNotification>>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        groups.forEach { (dateHeader, notifications) ->
            Text(dateHeader, color = InventraPurple, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            notifications.forEach { notification ->
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
                    listOf(
                        InventraNotification(
                            InventraStatusIconVariant.Danger,
                            "Leite integral vence amanhã!",
                            "Lote 000067",
                            "08:30",
                            LocalDate.now(),
                        ),
                    ),
            ),
    )
}
