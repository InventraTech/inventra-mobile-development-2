package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inventraoficial.inventra.R

@Composable
fun InventraMascotFab(
    painter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painter,
        contentDescription = "Abrir assistente",
        contentScale = ContentScale.Fit,
        modifier =
            modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraMascotFabPreview() {
    InventraMascotFab(painter = painterResource(R.drawable.ic_inventra_logo), onClick = {})
}
