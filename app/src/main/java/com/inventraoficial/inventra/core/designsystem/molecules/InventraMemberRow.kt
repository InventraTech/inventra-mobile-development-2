package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.inventraoficial.inventra.core.designsystem.atoms.InventraInitialsAvatar
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraMemberRow(
    initials: String,
    name: String,
    roleLabel: String,
    isOwner: Boolean,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InventraInitialsAvatar(initials = initials)
        Text(
            text = name,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
        )
        if (isOwner) {
            InventraBadge(text = roleLabel, variant = InventraBadgeVariant.Brand)
        } else {
            InventraBadge(text = roleLabel, variant = InventraBadgeVariant.Brand, outlined = true)
            InventraTextLink(text = "Remover", onClick = onRemoveClick, color = InventraDanger)
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraMemberRowPreview() {
    InventraMemberRow(
        initials = "PL",
        name = "Paula Lima",
        roleLabel = "Estoquista",
        isOwner = false,
        onRemoveClick = {},
    )
}
