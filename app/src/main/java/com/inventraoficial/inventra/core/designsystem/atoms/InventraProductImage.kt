package com.inventraoficial.inventra.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.inventraoficial.inventra.ui.theme.InventraPurple

@Composable
fun InventraProductImage(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
    iconSize: Dp = 24.dp,
    contentScale: ContentScale = ContentScale.Fit,
) {
    Box(
        modifier =
            modifier
                .clip(shape)
                .background(InventraPurple.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Inventory2,
            contentDescription = null,
            tint = InventraPurple,
            modifier = Modifier.size(iconSize),
        )
        if (photoUrl != null) {
            val fits = contentScale == ContentScale.Fit
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = contentScale,
                modifier =
                    Modifier
                        .matchParentSize()
                        .background(if (fits) Color.White else Color.Transparent)
                        .padding(if (fits) 4.dp else 0.dp),
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraProductImagePreview() {
    InventraProductImage(photoUrl = null, modifier = Modifier.size(48.dp))
}
