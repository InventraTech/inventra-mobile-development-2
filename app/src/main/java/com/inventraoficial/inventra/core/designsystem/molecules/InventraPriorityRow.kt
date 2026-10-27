package com.inventraoficial.inventra.core.designsystem.molecules

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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.atoms.InventraThumbnail
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

enum class InventraUrgency { Normal, Tomorrow }

@Composable
fun InventraPriorityRow(
    painter: Painter,
    title: String,
    statusText: String,
    urgency: InventraUrgency,
    modifier: Modifier = Modifier,
    batch: String,
    onClick: () -> Unit = {},
) {
    val statusColor = if (urgency == InventraUrgency.Tomorrow) InventraDanger else InventraPurple
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .heightIn(min = 72.dp)
                .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        InventraThumbnail(painter = painter, size = 48.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                color = InventraPurple,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(statusText, fontFamily = Montserrat, color = statusColor, fontSize = 12.sp)
            Text(batch, fontFamily = Montserrat, color = InventraPurple.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}

@Preview
@Composable
private fun InventraPriorityRowPreview() {
    InventraPriorityRow(
        painter = painterResource(R.drawable.ic_inventra_logo),
        title = "Arroz Camil",
        statusText = "Vence em 2 dias",
        urgency = InventraUrgency.Normal,
        batch = "Lote bla bla bla",
    )
}
