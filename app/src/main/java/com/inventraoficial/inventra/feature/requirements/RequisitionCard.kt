package com.inventraoficial.inventra.feature.requirements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun RequisitionCard(
    requisition: RequisitionSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape)
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), shape)
                .clickable(onClick = onClick)
                .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = requisition.title,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = InventraPurple,
            )
            Text(text = requisition.subtitle, fontFamily = Montserrat, fontSize = 13.sp, color = InventraMuted)
            if (requisition.details.isNotBlank()) {
                Text(text = requisition.details, fontFamily = Montserrat, fontSize = 13.sp)
            }
        }
        RequisitionStatusChip(status = requisition.status)
    }
}

@Composable
fun RequisitionStatusChip(
    status: RequisitionStatus,
    modifier: Modifier = Modifier,
) {
    val color =
        when (status) {
            RequisitionStatus.DRAFT -> InventraPurple
            RequisitionStatus.UNDER_REVIEW -> InventraGold
            RequisitionStatus.APPROVED -> InventraSuccess
            RequisitionStatus.REJECTED -> InventraDanger
            RequisitionStatus.CANCELLED -> InventraMuted
        }
    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = status.label,
            color = color,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
        )
    }
}

@Composable
fun RequisitionsLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = InventraPurple)
    }
}

@Composable
fun RequisitionsError(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, fontFamily = Montserrat, fontSize = 15.sp, textAlign = TextAlign.Center)
        InventraSecondaryButton(text = "Tentar novamente", onClick = onRetryClick)
    }
}

@Composable
fun RequisitionsEmpty(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            fontFamily = Montserrat,
            fontSize = 14.sp,
            color = InventraMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun RequisitionCardPreview() {
    Box(modifier = Modifier.background(Color.White).padding(16.dp)) {
        RequisitionCard(
            requisition =
                RequisitionSummary(
                    id = 12,
                    title = "Compra #12",
                    subtitle = "Maria Souza · 11/10/2026",
                    details = "Leite Integral, Arroz Camil e mais 2",
                    status = RequisitionStatus.UNDER_REVIEW,
                ),
            onClick = {},
        )
    }
}
