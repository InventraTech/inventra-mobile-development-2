package com.inventraoficial.inventra.feature.auth

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp

internal const val BOTTOM_SHAPE_HEIGHT_FRACTION = 0.22f
private const val TOP_SHAPE_WIDTH_FRACTION = 0.4f
internal val AuthContentMaxWidth = 520.dp
internal val MaxBottomShapeHeight = 220.dp
private val MinTopShapeSize = 200.dp
private val MaxTopShapeSize = 340.dp

internal fun topShapeSize(screenWidth: Dp): Dp {
    val size = screenWidth * TOP_SHAPE_WIDTH_FRACTION
    return size.coerceIn(MinTopShapeSize, MaxTopShapeSize)
}
