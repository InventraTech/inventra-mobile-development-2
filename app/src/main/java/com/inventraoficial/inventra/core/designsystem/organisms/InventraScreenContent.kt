package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.InventraScreenContent(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier =
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = ScreenContentMaxWidth)
                    .fillMaxWidth()
                    .fillMaxHeight(),
            content = content,
        )
    }
}

private val ScreenContentMaxWidth = 640.dp
