package com.inventraoficial.inventra.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

enum class InventraBadgeVariant { Brand, DateAlert, Gold }

@Composable
fun InventraBadge(
    text: String,
    variant: InventraBadgeVariant = InventraBadgeVariant.Brand,
    outlined: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val (baseColor, defaultContentColor) =
        when (variant) {
            InventraBadgeVariant.Brand -> InventraPurple to Color.White
            InventraBadgeVariant.DateAlert -> InventraDanger.copy(alpha = 0.14f) to InventraDanger
            InventraBadgeVariant.Gold -> InventraGold to Color.White
        }
    val containerColor = if (outlined) Color.White else baseColor
    val contentColor = if (outlined) baseColor else defaultContentColor

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(50))
                .then(if (outlined) Modifier.border(1.dp, baseColor, RoundedCornerShape(50)) else Modifier)
                .background(containerColor)
                .padding(horizontal = 11.dp, vertical = 4.dp),
    ) {
        Text(text = text, color = contentColor, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
    }
}

@Preview
@Composable
private fun InventraBadgePreview() {
    InventraBadge(text = "VPQ 50")
}
