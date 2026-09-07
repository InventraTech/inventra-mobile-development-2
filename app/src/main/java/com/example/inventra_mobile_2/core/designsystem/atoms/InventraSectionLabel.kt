package com.example.inventra_mobile_2.core.designsystem.atoms

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.ui.theme.InventraPurple
import com.example.inventra_mobile_2.ui.theme.Montserrat

@Composable
fun InventraSectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = InventraPurple,
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = modifier
    )
}

@Preview
@Composable
private fun InventraSectionLabelPreview() {
    InventraSectionLabel(text = "Dados do mês")
}