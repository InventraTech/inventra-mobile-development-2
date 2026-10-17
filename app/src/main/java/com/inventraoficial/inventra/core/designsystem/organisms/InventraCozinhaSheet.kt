package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBadge
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBadgeVariant
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRequestStatus
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSurface
import com.inventraoficial.inventra.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventraCozinhaSheet(
    name: String,
    location: String?,
    distance: String?,
    status: InventraCozinhaRequestStatus,
    onRequestClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = InventraSurface,
    ) {
        InventraCozinhaSheetContent(
            name = name,
            location = location,
            distance = distance,
            status = status,
            onRequestClick = onRequestClick,
            isLoading = isLoading,
            errorMessage = errorMessage,
            modifier = Modifier.navigationBarsPadding(),
        )
    }
}

@Composable
fun InventraCozinhaSheetContent(
    name: String,
    location: String?,
    distance: String?,
    status: InventraCozinhaRequestStatus,
    onRequestClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(InventraPurple.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = InventraPurple)
            }
            Text(
                text = name,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = InventraPurple,
                modifier = Modifier.weight(1f),
            )
            StatusBadge(status)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoLine(icon = Icons.Default.LocationOn, text = location ?: "Localização não informada")
            if (distance != null) {
                InfoLine(icon = Icons.Default.NearMe, text = "$distance de você")
            }
        }
        Text(
            text = status.helperText(),
            fontFamily = Montserrat,
            fontSize = 14.sp,
            color = InventraMuted,
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                fontFamily = Montserrat,
                fontSize = 14.sp,
                color = InventraDanger,
            )
        }
        InventraPrimaryButton(
            text = status.actionText(),
            onClick = onRequestClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = status != InventraCozinhaRequestStatus.Pendente,
            isLoading = isLoading,
        )
    }
}

@Composable
private fun StatusBadge(status: InventraCozinhaRequestStatus) {
    when (status) {
        InventraCozinhaRequestStatus.Disponivel -> Unit
        InventraCozinhaRequestStatus.Pendente -> InventraBadge(text = "Pendente", variant = InventraBadgeVariant.Gold)
        InventraCozinhaRequestStatus.Recusada ->
            InventraBadge(text = "Recusada", variant = InventraBadgeVariant.DateAlert)
    }
}

@Composable
private fun InfoLine(
    icon: ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = InventraMuted, modifier = Modifier.size(18.dp))
        Text(text = text, fontFamily = Montserrat, fontSize = 14.sp)
    }
}

private fun InventraCozinhaRequestStatus.actionText(): String =
    when (this) {
        InventraCozinhaRequestStatus.Disponivel -> "Solicitar entrada"
        InventraCozinhaRequestStatus.Pendente -> "Solicitação pendente"
        InventraCozinhaRequestStatus.Recusada -> "Solicitar novamente"
    }

private fun InventraCozinhaRequestStatus.helperText(): String =
    when (this) {
        InventraCozinhaRequestStatus.Disponivel ->
            "O supervisor da cozinha vai analisar seu pedido. Se ninguém responder, ele vence em 7 dias."
        InventraCozinhaRequestStatus.Pendente ->
            "Você já pediu para entrar nesta cozinha. Aguarde a resposta do supervisor."
        InventraCozinhaRequestStatus.Recusada ->
            "Seu pedido anterior para esta cozinha foi recusado. Você pode pedir de novo."
    }

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraCozinhaSheetDisponivelPreview() {
    InventraCozinhaSheetContent(
        name = "Restaurante Sabor Caseiro",
        location = "São Paulo, SP",
        distance = "3,2 km",
        status = InventraCozinhaRequestStatus.Disponivel,
        onRequestClick = {},
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraCozinhaSheetPendentePreview() {
    InventraCozinhaSheetContent(
        name = "Tung Tung Tung Sahur",
        location = "Rio de Janeiro, RJ",
        distance = null,
        status = InventraCozinhaRequestStatus.Pendente,
        onRequestClick = {},
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraCozinhaSheetRecusadaPreview() {
    InventraCozinhaSheetContent(
        name = "Instituto J&F",
        location = null,
        distance = null,
        status = InventraCozinhaRequestStatus.Recusada,
        onRequestClick = {},
        errorMessage = "Você já tem uma solicitação de entrada pendente.",
    )
}
