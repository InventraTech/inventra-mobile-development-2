package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.inventraoficial.inventra.core.designsystem.atoms.InventraInitialsAvatar
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraPendingMemberRow(
    initials: String,
    name: String,
    roleLabel: String,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(InventraGold.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InventraInitialsAvatar(initials = initials)
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
                text = "Solicitou entrada como $roleLabel",
                fontFamily = Montserrat,
                fontSize = 12.sp,
                color = Color.Gray,
            )
        }
        RoundIconButton(icon = Icons.Default.Check, background = InventraSuccess, onClick = onAcceptClick)
        RoundIconButton(icon = Icons.Default.Close, background = InventraDanger, onClick = onRejectClick)
    }
}

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    background: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(36.dp)
                .background(background, CircleShape)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraPendingMemberRowPreview() {
    InventraPendingMemberRow(
        initials = "MR",
        name = "Mauro Rosa",
        roleLabel = "Estoquista",
        onAcceptClick = {},
        onRejectClick = {},
    )
}
