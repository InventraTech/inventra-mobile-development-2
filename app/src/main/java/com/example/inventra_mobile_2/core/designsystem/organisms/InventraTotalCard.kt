package com.example.inventra_mobile_2.core.designsystem.organisms

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.core.designsystem.atoms.InventraSectionLabel
import com.example.inventra_mobile_2.ui.theme.InventraPurple
import com.example.inventra_mobile_2.ui.theme.Montserrat

@Composable
fun InventraTotalCard(
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, InventraPurple.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        InventraSectionLabel(text = label)
        Text(value, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 28.sp)
        Text(caption, fontFamily = Montserrat, fontSize = 13.sp, color = InventraPurple.copy(alpha = 0.6f))
    }
}

@Preview
@Composable
private fun InventraTotalCardPreview() {
    InventraTotalCard(label = "Quantidade total", value = "18 L", caption = "Em 3 lotes de 6 litros.")
}