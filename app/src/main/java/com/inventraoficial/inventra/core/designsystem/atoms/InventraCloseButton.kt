package com.inventraoficial.inventra.core.designsystem.atoms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun InventraCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Fechar",
        tint = Color.White,
        modifier =
            modifier
                .size(24.dp)
                .clickable(onClick = onClick),
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraCloseButtonPreview() {
    InventraCloseButton(onClick = {})
}
