package com.inventraoficial.inventra.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraInitialsAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .background(InventraPurple.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            color = InventraPurple,
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraInitialsAvatarPreview() {
    InventraInitialsAvatar(initials = "JS")
}
