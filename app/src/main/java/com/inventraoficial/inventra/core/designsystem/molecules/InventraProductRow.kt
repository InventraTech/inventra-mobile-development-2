package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
    metadata: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InventraThumbnail(painter = painter)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                color = InventraPurple,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(metadata, fontFamily = Montserrat, color = InventraPurple.copy(alpha = 0.6f), fontSize = 13.sp)
        }
        Text("›", fontWeight = FontWeight.Bold, color = InventraPurple.copy(alpha = 0.4f), fontSize = 20.sp)
    }
}

@Preview
@Composable
private fun InventraProductRowPreview() {
    InventraProductRow(
        painter = painterResource(R.drawable.ic_inventra_logo),
        title = "Leite Integral",
        metadata = "18 L · 3 lotes",
        onClick = {},
    )
}
