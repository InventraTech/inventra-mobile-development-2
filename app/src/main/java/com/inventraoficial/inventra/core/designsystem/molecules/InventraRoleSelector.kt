package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSelectableCard

enum class InventraUserRole {
    Supervisor,
    Estoquista,
}

@Composable
fun InventraRoleSelector(
    selectedRole: InventraUserRole?,
    onRoleSelect: (InventraUserRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InventraSelectableCard(
            icon = Icons.Default.VerifiedUser,
            label = "Sou supervisor",
            selected = selectedRole == InventraUserRole.Supervisor,
            onClick = { onRoleSelect(InventraUserRole.Supervisor) },
            modifier = Modifier.weight(1f),
        )
        InventraSelectableCard(
            icon = Icons.Default.Inventory2,
            label = "Sou estoquista",
            selected = selectedRole == InventraUserRole.Estoquista,
            onClick = { onRoleSelect(InventraUserRole.Estoquista) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
private fun InventraRoleSelectorPreview() {
    InventraRoleSelector(
        selectedRole = InventraUserRole.Supervisor,
        onRoleSelect = {},
    )
}
