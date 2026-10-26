package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

// Moldura do scan sobre a câmera, com um painel embaixo para o que a tela precisar mostrar.
@Composable
fun InventraScanOverlay(
    modifier: Modifier = Modifier,
    frameColor: Color = InventraPurple,
    panel: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val frameWidth = 280.dp.toPx()
                val frameHeight = 140.dp.toPx()
                drawRect(color = Color.Black.copy(alpha = 0.85f))
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft =
                        Offset(
                            (size.width - frameWidth) / 2f,
                            (size.height - frameHeight) / 2f,
                        ),
                    size = Size(frameWidth, frameHeight),
                    blendMode = BlendMode.Clear,
                )
            }
            Canvas(modifier = Modifier.size(width = 280.dp, height = 140.dp)) {
                val corner = 28.dp.toPx()
                val stroke = 4.dp.toPx()
                val w = size.width
                val h = size.height
                drawLine(frameColor, Offset(0f, corner), Offset(0f, 0f), stroke)
                drawLine(frameColor, Offset(0f, 0f), Offset(corner, 0f), stroke)
                drawLine(frameColor, Offset(w - corner, 0f), Offset(w, 0f), stroke)
                drawLine(frameColor, Offset(w, 0f), Offset(w, corner), stroke)
                drawLine(frameColor, Offset(0f, h - corner), Offset(0f, h), stroke)
                drawLine(frameColor, Offset(0f, h), Offset(corner, h), stroke)
                drawLine(frameColor, Offset(w - corner, h), Offset(w, h), stroke)
                drawLine(frameColor, Offset(w, h - corner), Offset(w, h), stroke)
            }
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp)
                    .background(InventraBackground)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            content = panel,
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraScanOverlayPreview() {
    InventraScanOverlay {
        Text("Aponte a câmera para o código de barras", fontFamily = Montserrat, fontSize = 15.sp)
    }
}
