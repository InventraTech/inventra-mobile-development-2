package com.inventraoficial.inventra.feature.requirements.history.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.atoms.InventraThumbnail
import com.inventraoficial.inventra.feature.requirements.list.ui.RequirementStatus
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.Montserrat

private const val ICON_SIZE = 24

@Composable
fun HistoryItemCard(
    painter: Painter,
    title: String,
    description: String,
    status: RequirementStatus,
    modifier: Modifier = Modifier,
) {
    val (
        backgroundColor,
        iconColor,
        statusIcon,
    ) = when (status) {
        RequirementStatus.ACCEPTED ->
            Triple(
                InventraSuccess.copy(alpha = 0.1f),
                InventraSuccess,
                Icons.Default.Check,
            )
        RequirementStatus.REJECTED ->
            Triple(
                InventraDanger.copy(alpha = 0.1f),
                InventraDanger,
                Icons.Default.Close,
            )
        RequirementStatus.PENDING ->
            Triple(
                Color.Gray.copy(alpha = 0.1f),
                Color.Gray,
                Icons.Default.Close,
            )
    }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(12.dp))
                .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InventraThumbnail(painter = painter)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = InventraPurple,
            )
            Text(
                text = description,
                fontFamily = Montserrat,
                fontSize = 12.sp,
                color = Color.Gray,
            )
        }

        Icon(
            imageVector = statusIcon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(ICON_SIZE.dp),
        )
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun HistoryItemCardAcceptedPreview() {
    HistoryItemCard(
        painter = painterResource(R.drawable.ic_inventra_logo),
        title = "Baião - Arroz Camil",
        description = "Solicitação aprovada",
        status = RequirementStatus.ACCEPTED,
    )
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun HistoryItemCardRejectedPreview() {
    HistoryItemCard(
        painter = painterResource(R.drawable.ic_inventra_logo),
        title = "Eduardo Passos",
        description = "Entrada como Estoquista recusada",
        status = RequirementStatus.REJECTED,
    )
}
