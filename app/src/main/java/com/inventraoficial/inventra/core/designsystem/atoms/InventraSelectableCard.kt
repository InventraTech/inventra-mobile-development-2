package com.inventraoficial.inventra.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraSelectableCard(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) InventraPurple else InventraPurple.copy(alpha = 0.15f)
    val contentColor = if (selected) InventraPurple else Color.Gray
    val backgroundColor = if (selected) InventraPurple.copy(alpha = 0.06f) else Color.White

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
                .background(backgroundColor, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor)
        Text(
            text = label,
            fontFamily = Montserrat,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            color = contentColor,
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraSelectableCardPreview() {
    InventraSelectableCard(
        icon = Icons.Default.Check,
        label = "Sou supervisor",
        selected = true,
        onClick = {},
    )
}
