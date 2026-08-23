package com.example.inventra_mobile_2.core.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.ui.theme.InventraGold

@Composable
fun InventraQrScanFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(InventraGold)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("▦", fontSize = 22.sp, color = Color(0xFF3A2900))
    }
}

@Preview
@Composable
private fun InventraQrScanFabPreview() {
    InventraQrScanFab(onClick = {})
}