package com.example.inventra_mobile_2.core.designsystem.atoms

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.ui.theme.InventraPurple
import com.example.inventra_mobile_2.ui.theme.Montserrat

@Composable
fun InventraTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showArrow: Boolean = false
) {
    Text(
        text = if (showArrow) "$text →" else text,
        color = InventraPurple,
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Preview
@Composable
private fun InventraTextLinkPreview() {
    InventraTextLink(text = "Ver todos os lotes", onClick = {}, showArrow = true)
}