package com.example.inventra_mobile_2.core.designsystem.atoms

import androidx.compose.foundation.background
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
import com.example.inventra_mobile_2.ui.theme.InventraDanger
import com.example.inventra_mobile_2.ui.theme.InventraPurple
import com.example.inventra_mobile_2.ui.theme.Montserrat

enum class InventraBadgeVariant { Brand, DateAlert }

@Composable
fun InventraBadge(
    text: String,
    variant: InventraBadgeVariant = InventraBadgeVariant.Brand,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (variant) {
        InventraBadgeVariant.Brand -> InventraPurple to Color.White
        InventraBadgeVariant.DateAlert -> InventraDanger.copy(alpha = 0.14f) to InventraDanger
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 11.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = contentColor, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
    }
}

@Preview
@Composable
private fun InventraBadgePreview() {
    InventraBadge(text = "VPQ 50")
}