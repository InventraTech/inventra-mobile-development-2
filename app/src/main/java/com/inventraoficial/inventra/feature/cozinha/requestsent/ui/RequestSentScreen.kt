package com.inventraoficial.inventra.feature.cozinha.requestsent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSectionLabel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRequestStatus
import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScreenContent
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.feature.cozinha.select.ui.CozinhaListItem
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat
import kotlinx.coroutines.delay

private const val AUTO_REFRESH_MS = 30_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestSentScreen(
    uiState: RequestSentUiState,
    onRefreshClick: () -> Unit,
    onSeeCozinhasClick: () -> Unit,
    onRetryClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            title = {
                Text(
                    "Minhas solicitações",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
            },
        )
        InventraScreenContent {
            val contentModifier = Modifier.weight(1f).fillMaxWidth()
            when {
                uiState.isLoading ->
                    Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = InventraPurple)
                    }
                uiState.errorMessage != null ->
                    Column(
                        modifier = contentModifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    ) {
                        Text(
                            uiState.errorMessage,
                            fontFamily = Montserrat,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                        )
                        InventraPrimaryButton(
                            text = "Tentar novamente",
                            onClick = onRetryClick,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        InventraTextLink(text = "Sair da conta", onClick = onLogoutClick)
                    }
                else ->
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefreshClick,
                        modifier = contentModifier,
                    ) {
                        RequestsContent(
                            uiState = uiState,
                            onRefreshClick = onRefreshClick,
                            onSeeCozinhasClick = onSeeCozinhasClick,
                            onLogoutClick = onLogoutClick,
                        )
                    }
            }
        }
    }
}

@Composable
private fun RequestsContent(
    uiState: RequestSentUiState,
    onRefreshClick: () -> Unit,
    onSeeCozinhasClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CurrentRequestCard(uiState)
        InventraPrimaryButton(
            text = if (uiState.canRequestAnother) "Pedir entrada em outra cozinha" else "Ver cozinhas",
            onClick = onSeeCozinhasClick,
            modifier = Modifier.fillMaxWidth(),
        )
        InventraSecondaryButton(
            text = if (uiState.isRefreshing) "Atualizando..." else "Atualizar status",
            onClick = onRefreshClick,
            modifier = Modifier.fillMaxWidth(),
        )
        if (uiState.otherRequests.isNotEmpty()) {
            InventraSectionLabel(text = "Outras cozinhas que você pediu", modifier = Modifier.padding(top = 8.dp))
            uiState.otherRequests.forEach { cozinha ->
                InventraCozinhaRow(
                    name = cozinha.name,
                    subtitle = cozinha.subtitle,
                    status = cozinha.status,
                    onClick = onSeeCozinhasClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        InventraTextLink(
            text = "Sair da conta",
            onClick = onLogoutClick,
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp),
        )
    }
}

@Composable
private fun CurrentRequestCard(uiState: RequestSentUiState) {
    val isPending = uiState.status == RequestSentStatus.Pending
    val color = if (isPending) InventraGold else InventraDanger
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isPending) Icons.Default.HourglassTop else Icons.Default.Block,
                    contentDescription = null,
                    tint = color,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isPending) "Aguardando aprovação" else "Pedido recusado",
                    fontFamily = Montserrat,
                    fontSize = 13.sp,
                    color = color,
                )
                Text(
                    uiState.cozinhaName,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = InventraPurple,
                )
            }
        }
        uiState.sentAt?.let { CardLine("Enviado em $it") }
        if (isPending) {
            uiState.expiresAt?.let { date ->
                CardLine(listOfNotNull(date, uiState.expiresIn).joinToString(" · ", prefix = "Prazo: "))
            }
            CardNote(
                "Enquanto este pedido estiver pendente, não dá para pedir entrada em outra cozinha. " +
                    "Se o supervisor não responder, ele vence sozinho no prazo.",
            )
        } else {
            uiState.rejectionReason?.let { CardLine("Motivo: $it") }
            CardNote("Você pode pedir entrada em outra cozinha, ou nesta de novo.")
        }
    }
}

@Composable
private fun CardLine(text: String) {
    Text(text, fontFamily = Montserrat, fontSize = 14.sp)
}

@Composable
private fun CardNote(text: String) {
    Text(text, fontFamily = Montserrat, fontSize = 13.sp, color = InventraMuted)
}

@Composable
fun RequestSentRoute(
    navigator: Navigator,
    viewModel: RequestSentViewModel = viewModel(factory = RequestSentViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Confere o pedido ao voltar para o app e a cada 30 segundos com a tela aberta.
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                viewModel.onAutoRefresh()
                delay(AUTO_REFRESH_MS)
            }
        }
    }

    LaunchedEffect(uiState.destination) {
        when (uiState.destination) {
            RequestSentDestination.Home -> navigator.navigateHome()
            RequestSentDestination.SelectCozinha -> navigator.replaceStack(Screen.SelectCozinha)
            RequestSentDestination.Login -> navigator.replaceStack(Screen.Login)
            null -> Unit
        }
    }

    RequestSentScreen(
        uiState = uiState,
        onRefreshClick = viewModel::onRefreshClick,
        // Abre por cima: voltar do mapa traz de volta para cá.
        onSeeCozinhasClick = { navigator.navigate(Screen.SelectCozinha) },
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
                sentAt = "17/10/2026",
                expiresAt = "24/10/2026",
                expiresIn = "vence em 7 dias",
                otherRequests =
                    listOf(
                        CozinhaListItem(
                            id = 2,
                            name = "Sabor Filial Sul",
                            location = "São Paulo, SP",
                            distance = null,
                            status = InventraCozinhaRequestStatus.Recusada,
                        ),
                    ),
            ),
        onRefreshClick = {},
        onSeeCozinhasClick = {},
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
                sentAt = "10/10/2026",
                rejectionReason = "Não faz parte da equipe",
            ),
        onRefreshClick = {},
        onSeeCozinhasClick = {},
        onRetryClick = {},
        onLogoutClick = {},
    )
}
