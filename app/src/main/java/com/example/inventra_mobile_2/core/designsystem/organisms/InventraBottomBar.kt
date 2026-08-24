package com.example.inventra_mobile_2.core.designsystem.organisms

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.inventra_mobile_2.ui.theme.InventraNavInk
import com.example.inventra_mobile_2.ui.theme.InventraPurple

enum class InventraBottomDestination(val icon: ImageVector) {
    Home(Icons.Default.Home),
    Stock(Icons.Default.Inventory2),
    History(Icons.Default.History),
    Notifications(Icons.Default.Notifications)
}

private class InventraNotchedBarShape(
    private val cornerRadius: Dp,
    private val notchRadius: Dp,
    private val notchMargin: Dp,
    private val notchCenterFraction: Float
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cornerPx = with(density) { cornerRadius.toPx() }
        val notchPx = with(density) { (notchRadius + notchMargin).toPx() }
        val notchCenterX = size.width * notchCenterFraction

        val barPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, size.width, size.height),
                    topLeft = CornerRadius(cornerPx, cornerPx),
                    topRight = CornerRadius(cornerPx, cornerPx),
                    bottomRight = CornerRadius.Zero,
                    bottomLeft = CornerRadius.Zero
                )
            )
        }
        val notchPath = Path().apply {
            addOval(Rect(center = Offset(notchCenterX, 0f), radius = notchPx))
        }
        val resultPath = Path()
        resultPath.op(barPath, notchPath, PathOperation.Difference)
        return Outline.Generic(resultPath)
    }
}

@Composable
fun InventraBottomBar(
    selected: InventraBottomDestination,
    onSelect: (InventraBottomDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val destinations = InventraBottomDestination.entries
    val selectedIndex = destinations.indexOf(selected)
    val notchCenterFraction = (selectedIndex + 0.5f) / destinations.size

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val circleSize = 48.dp
        val slotWidth = maxWidth / destinations.size
        val circleX = slotWidth * selectedIndex + (slotWidth - circleSize) / 2

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    InventraNavInk,
                    InventraNotchedBarShape(
                        cornerRadius = 22.dp,
                        notchRadius = 24.dp,
                        notchMargin = 8.dp,
                        notchCenterFraction = notchCenterFraction
                    )
                )
                .padding(vertical = 14.dp)
        ) {
            destinations.forEach { destination ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = { onSelect(destination) }),
                    contentAlignment = Alignment.Center
                ) {
                    if (destination != selected) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .offset(x = circleX, y = (-18).dp)
                .size(circleSize)
                .clip(CircleShape)
                .background(InventraPurple)
                .clickable(onClick = { onSelect(selected) }),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = selected.icon,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Preview
@Composable
private fun InventraBottomBarPreview() {
    InventraBottomBar(selected = InventraBottomDestination.Home, onSelect = {})
}