package com.example.inventra_mobile_2.core.designsystem.organisms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.core.designsystem.atoms.InventraPrimaryButton
import com.example.inventra_mobile_2.ui.theme.InventraPurple
import com.example.inventra_mobile_2.ui.theme.Montserrat

@Composable
fun InventraScanOverlay(
    instruction: String,
    onScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(220.dp)) {
                val corner = 28.dp.toPx()
                val stroke = 4.dp.toPx()
                val w = size.width
                val h = size.height
                drawLine(InventraPurple, Offset(0f, corner), Offset(0f, 0f), stroke)
                drawLine(InventraPurple, Offset(0f, 0f), Offset(corner, 0f), stroke)
                drawLine(InventraPurple, Offset(w - corner, 0f), Offset(w, 0f), stroke)
                drawLine(InventraPurple, Offset(w, 0f), Offset(w, corner), stroke)
                drawLine(InventraPurple, Offset(0f, h - corner), Offset(0f, h), stroke)
                drawLine(InventraPurple, Offset(0f, h), Offset(corner, h), stroke)
                drawLine(InventraPurple, Offset(w - corner, h), Offset(w, h), stroke)
                drawLine(InventraPurple, Offset(w, h - corner), Offset(w, h), stroke)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(instruction, fontFamily = Montserrat, fontSize = 15.sp)
            InventraPrimaryButton(text = "Escanear", onClick = onScan, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview
@Composable
private fun InventraScanOverlayPreview() {
    InventraScanOverlay(instruction = "Posicione o código de barras na moldura", onScan = {})
}