package com.example.inventra_mobile_2.core.designsystem.molecules

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
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
import com.example.inventra_mobile_2.ui.theme.InventraDanger
import com.example.inventra_mobile_2.ui.theme.InventraPurple
import com.example.inventra_mobile_2.ui.theme.InventraSuccess
import com.example.inventra_mobile_2.ui.theme.Montserrat

enum class InventraStatTone { Positive, Negative }

@Composable
fun InventraStatTile(
    label: String,
    value: String,
    trend: String,
    tone: InventraStatTone,
    modifier: Modifier = Modifier
) {
    val valueColor = if (tone == InventraStatTone.Positive) InventraSuccess else InventraDanger
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, InventraPurple.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        InventraSectionLabel(text = label)
        Text(value, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = valueColor)
        Text(trend, fontFamily = Montserrat, fontSize = 10.5.sp, color = InventraPurple.copy(alpha = 0.5f))
    }
}

@Preview
@Composable
private fun InventraStatTilePreview() {
    InventraStatTile(label = "Economia", value = "R$ 6767,00", trend = "↑ 67% vs mês anterior", tone = InventraStatTone.Positive)
}