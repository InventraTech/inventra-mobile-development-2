package com.inventraoficial.inventra.feature.requirements.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.Montserrat

private const val BUTTON_SIZE = 40
private const val BUTTON_PADDING = 8
private const val CARD_PADDING = 12
private const val SPACING = 8

@Composable
fun RequirementAcceptanceCard(
    painter: Painter,
    title: String,
    description: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(InventraGold, RoundedCornerShape(12.dp))
                .padding(CARD_PADDING.dp),
        horizontalArrangement = Arrangement.spacedBy(SPACING.dp),
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
                color = Color.Black,
            )
            Text(
                text = description,
                fontFamily = Montserrat,
                fontSize = 12.sp,
                color = Color.Gray,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(SPACING.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Accept",
                tint = Color.White,
                modifier =
                    Modifier
                        .size(BUTTON_SIZE.dp)
                        .background(InventraSuccess, RoundedCornerShape(50))
                        .clickable(onClick = onAccept)
                        .padding(BUTTON_PADDING.dp),
            )

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Reject",
                tint = Color.White,
                modifier =
                    Modifier
                        .size(BUTTON_SIZE.dp)
                        .background(InventraDanger, RoundedCornerShape(50))
                        .clickable(onClick = onReject)
                        .padding(BUTTON_PADDING.dp),
            )
        }
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun RequirementAcceptanceCardPreview() {
    RequirementAcceptanceCard(
        painter = painterResource(R.drawable.ic_inventra_logo),
        title = "Baião - Arroz Camil",
        description = "Solicitação de requisição",
        onAccept = {},
        onReject = {},
    )
}
