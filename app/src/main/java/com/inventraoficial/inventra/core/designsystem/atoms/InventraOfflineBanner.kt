package com.inventraoficial.inventra.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.Montserrat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun InventraOfflineBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = message,
        fontFamily = Montserrat,
        fontSize = 13.sp,
        color = InventraInk,
        modifier =
            modifier
                .fillMaxWidth()
                .background(InventraGold.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
    )
}

fun offlineMessage(
    subject: String,
    savedAtMillis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    val savedAt = Instant.ofEpochMilli(savedAtMillis).atZone(zone)
    return "Sem conexão. Mostrando $subject em ${savedAt.format(savedAtFormatter)}."
}

private val savedAtFormatter = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm")
