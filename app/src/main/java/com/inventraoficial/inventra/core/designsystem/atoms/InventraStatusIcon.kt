package com.inventraoficial.inventra.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.Montserrat

enum class InventraStatusIconVariant { Success, Danger, Info, Action }

@Composable
fun InventraStatusIcon(
    variant: InventraStatusIconVariant,
    modifier: Modifier = Modifier,
) {
    val (background, glyph) =
        when (variant) {
            InventraStatusIconVariant.Success -> InventraSuccess to "✓"
            InventraStatusIconVariant.Danger -> InventraDanger to "!"
            InventraStatusIconVariant.Info -> InventraGold to "i"
            InventraStatusIconVariant.Action -> InventraPurple to "🛒"
        }
    Box(
        modifier = modifier.size(34.dp).clip(CircleShape).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = glyph, color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
    }
}

@Preview
@Composable
private fun InventraStatusIconPreview() {
    InventraStatusIcon(variant = InventraStatusIconVariant.Danger)
}
