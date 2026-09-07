package com.example.inventra_mobile_2.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.inventra_mobile_2.ui.theme.InventraGold
import com.example.inventra_mobile_2.ui.theme.InventraPurple

private val InventraHexagonShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.25f, h * 0.06f)
    lineTo(w * 0.75f, h * 0.06f)
    lineTo(w, h * 0.5f)
    lineTo(w * 0.75f, h * 0.94f)
    lineTo(w * 0.25f, h * 0.94f)
    lineTo(0f, h * 0.5f)
    close()
}

@Composable
fun InventraBrandIcon(
    modifier: Modifier = Modifier,
    badgeContent: @Composable () -> Unit = {}
) {
    Box(modifier = modifier.size(64.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(InventraHexagonShape)
                .background(InventraPurple),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp, 24.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(InventraGold)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(22.dp)
                .clip(CircleShape)
                .background(InventraPurple)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            badgeContent()
        }
    }
}

@Preview
@Composable
private fun InventraBrandIconPreview() {
    InventraBrandIcon()
}