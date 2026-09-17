package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraProductRow(
    painter: Painter,
    title: String,
    quantity: String,
    batchInfo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .heightIn(min = 108.dp)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        InventraThumbnail(painter = painter, size = 64.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                color = InventraPurple,
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(quantity, fontFamily = Montserrat, color = InventraPurple.copy(alpha = 0.6f), fontSize = 17.sp)
            Text(batchInfo, fontFamily = Montserrat, color = InventraPurple.copy(alpha = 0.6f), fontSize = 17.sp)
        }
        Text("›", fontWeight = FontWeight.Bold, color = InventraPurple.copy(alpha = 0.4f), fontSize = 22.sp)
    }
}

@Preview
@Composable
private fun InventraProductRowPreview() {
    InventraProductRow(
        painter = painterResource(R.drawable.ic_inventra_logo),
        title = "Leite Integral",
        quantity = "18 L",
        batchInfo = "3 lotes",
        onClick = {},
    )
}
