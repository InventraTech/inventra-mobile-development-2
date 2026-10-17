package com.inventraoficial.inventra.feature.cozinha.requestsent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun RequestSentScreen(
    uiState: RequestSentUiState,
    onRefreshClick: () -> Unit,
    onChooseAnotherClick: () -> Unit,
    onRetryClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground)
                .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(color = InventraPurple)
            uiState.errorMessage != null ->
                StatusContent(
                    icon = Icons.Default.Block,
                    color = InventraDanger,
                    title = "Não foi possível carregar",
                    message = uiState.errorMessage,
                    actionText = "Tentar novamente",
                    onActionClick = onRetryClick,
                    onLogoutClick = onLogoutClick,
                )
            uiState.status == RequestSentStatus.Rejected ->
                StatusContent(
                    icon = Icons.Default.Block,
                    color = InventraDanger,
                    title = "Solicitação recusada",
                    message = uiState.rejectedMessage(),
                    actionText = "Escolher outra cozinha",
                    onActionClick = onChooseAnotherClick,
                    onLogoutClick = onLogoutClick,
                )
            else ->
                StatusContent(
                    icon = Icons.Default.HourglassTop,
                    color = InventraGold,
                    title = "Solicitação enviada",
                    message = uiState.pendingMessage(),
                    actionText = "Atualizar status",
                    onActionClick = onRefreshClick,
                    onLogoutClick = onLogoutClick,
                    isActionLoading = uiState.isRefreshing,
                )
        }
    }
}

@Composable
private fun StatusContent(
    icon: ImageVector,
    color: Color,
    title: String,
    message: String,
    actionText: String,
    onActionClick: () -> Unit,
    onLogoutClick: () -> Unit,
    isActionLoading: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(96.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(48.dp),
            )
        }
        Text(
            text = title,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            text = message,
            fontFamily = Montserrat,
            fontSize = 14.sp,
            color = InventraMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        InventraPrimaryButton(
            text = actionText,
            onClick = onActionClick,
            isLoading = isActionLoading,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
        )
        InventraTextLink(
            text = "Sair da conta",
            onClick = onLogoutClick,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

private fun RequestSentUiState.pendingMessage(): String {
    val base =
        "Seu pedido para entrar na cozinha $cozinhaName foi enviado. Agora é só aguardar o supervisor aprovar."
    return if (expiresAt == null) base else "$base\n\nSe ninguém responder, o pedido vence em $expiresAt."
}

private fun RequestSentUiState.rejectedMessage(): String {
    val base = "O supervisor da cozinha $cozinhaName recusou seu pedido."
    return if (rejectionReason == null) base else "$base\n\nMotivo: $rejectionReason"
}

@Composable
fun RequestSentRoute(
    navigator: Navigator,
    viewModel: RequestSentViewModel = viewModel(factory = RequestSentViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.destination) {
        when (uiState.destination) {
            RequestSentDestination.Home -> navigator.navigateTopLevel(Screen.Home)
            RequestSentDestination.SelectCozinha -> navigator.replaceStack(Screen.SelectCozinha)
            RequestSentDestination.Login -> navigator.replaceStack(Screen.Login)
            null -> Unit
        }
    }

    RequestSentScreen(
        uiState = uiState,
        onRefreshClick = viewModel::onRefreshClick,
        onChooseAnotherClick = viewModel::onChooseAnotherClick,
        onRetryClick = viewModel::onRetryClick,
        onLogoutClick = viewModel::onLogoutClick,
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun RequestSentPendingPreview() {
    RequestSentScreen(
        uiState =
            RequestSentUiState(
                isLoading = false,
                cozinhaName = "Restaurante Sabor Caseiro",
                expiresAt = "18/10/2026",
            ),
        onRefreshClick = {},
        onChooseAnotherClick = {},
        onRetryClick = {},
        onLogoutClick = {},
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun RequestSentRejectedPreview() {
    RequestSentScreen(
        uiState =
            RequestSentUiState(
                isLoading = false,
                cozinhaName = "Restaurante Sabor Caseiro",
                status = RequestSentStatus.Rejected,
                rejectionReason = "Não faz parte da equipe",
            ),
        onRefreshClick = {},
        onChooseAnotherClick = {},
        onRetryClick = {},
        onLogoutClick = {},
    )
}
