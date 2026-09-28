package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBadge
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBadgeVariant
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraCozinhaRow(
    name: String,
    subtitle: String,
    status: InventraCozinhaRequestStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .background(InventraPurple.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = InventraPurple)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = InventraPurple)
            Text(subtitle, fontFamily = Montserrat, fontSize = 13.sp)
        }
        when (status) {
            InventraCozinhaRequestStatus.Disponivel ->
                InventraBadge(text = "Disponível", variant = InventraBadgeVariant.Brand, outlined = true)
            InventraCozinhaRequestStatus.Pendente ->
                InventraBadge(text = "Pendente", variant = InventraBadgeVariant.Gold)
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraCozinhaRowPreview() {
    InventraCozinhaRow(
        name = "Restaurante Sabor Caseiro",
        subtitle = "Matriz · São Paulo, SP",
        status = InventraCozinhaRequestStatus.Disponivel,
        onClick = {},
    )
}
