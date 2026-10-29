package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBadge
import com.inventraoficial.inventra.core.designsystem.atoms.InventraStarRating
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSupplierImage
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraSupplierRow(
    photoUrl: String?,
    name: String,
    rating: Int,
    badgeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        InventraSupplierImage(photoUrl = photoUrl)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                name,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                color = InventraPurple,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            InventraStarRating(rating = rating)
            InventraBadge(text = badgeText)
        }
    }
}

@Preview
@Composable
private fun InventraSupplierRowPreview() {
    InventraSupplierRow(
        photoUrl = null,
        name = "Distribuidora QA Alimentos Ltda",
        rating = 4,
        badgeText = "11.222.333/0001-81",
        onClick = {},
    )
}
