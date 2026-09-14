package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBadge
import com.inventraoficial.inventra.core.designsystem.atoms.InventraStarRating
import com.inventraoficial.inventra.core.designsystem.atoms.InventraThumbnail
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraSupplierRow(
    painter: Painter,
    name: String,
    rating: Int,
    badgeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InventraThumbnail(painter = painter, shape = CircleShape)
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = InventraPurple)
            InventraStarRating(rating = rating)
        }
        InventraBadge(text = badgeText)
    }
}

@Preview
@Composable
private fun InventraSupplierRowPreview() {
    InventraSupplierRow(
        painter = painterResource(R.drawable.ic_inventra_logo),
        name = "Seara Alimentos",
        rating = 5,
        badgeText = "VPQ 50",
        onClick = {},
    )
}
