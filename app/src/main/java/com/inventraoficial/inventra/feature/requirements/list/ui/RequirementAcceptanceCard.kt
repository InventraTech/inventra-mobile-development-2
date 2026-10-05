package com.inventraoficial.inventra.feature.requirements.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

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
                .background(InventraGold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(InventraSuccess, CircleShape)
                    .clickable { onAccept() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Aceitar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(InventraDanger, CircleShape)
                    .clickable { onReject() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Recusar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun RequirementAcceptanceCardPreview() {
    RequirementAcceptanceCard(
        painter = painterResource(R.drawable.ic_inventra_logo),
        title = "Baião - Arroz Camil",
        description = "Solicitação entrada como Estoquista",
        onAccept = {},
        onReject = {},
    )
}
