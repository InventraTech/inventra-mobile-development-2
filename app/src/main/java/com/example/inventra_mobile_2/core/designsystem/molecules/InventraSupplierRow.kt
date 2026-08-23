package com.example.inventra_mobile_2.core.designsystem.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.inventra_mobile_2.R
import com.example.inventra_mobile_2.core.designsystem.atoms.InventraBadge
import com.example.inventra_mobile_2.core.designsystem.atoms.InventraStarRating
import com.example.inventra_mobile_2.core.designsystem.atoms.InventraThumbnail
import com.example.inventra_mobile_2.ui.theme.InventraPurple
import com.example.inventra_mobile_2.ui.theme.Montserrat

@Composable
fun InventraSupplierRow(
    painter: Painter,
    name: String,
    rating: Int,
    badgeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
        onClick = {}
    )
}