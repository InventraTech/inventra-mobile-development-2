package com.inventraoficial.inventra.feature.profile.members.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraCloseButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraConfirmDialog
import com.inventraoficial.inventra.core.designsystem.molecules.InventraMemberRow
import com.inventraoficial.inventra.core.designsystem.molecules.InventraPendingMemberRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun ManageMembersScreen(
    uiState: ManageMembersUiState,
    onAcceptClick: (PendingMemberRequest) -> Unit,
    onRejectClick: (PendingMemberRequest) -> Unit,
    onRejectReasonChange: (String) -> Unit,
    onRejectConfirm: () -> Unit,
    onRejectDismiss: () -> Unit,
    onRetryClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    uiState.rejectTarget?.let { target ->
        InventraConfirmDialog(
            title = "Recusar solicitação?",
            message = "${target.name} não vai entrar na cozinha.",
            confirmText = "Recusar",
            onConfirm = onRejectConfirm,
            onDismiss = onRejectDismiss,
            isDestructive = true,
        ) {
            InventraLabeledField(
                label = "Motivo (opcional)",
                value = uiState.rejectReason,
                onValueChange = onRejectReasonChange,
                placeholder = "Ex.: não faz parte da equipe",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        InventraTopBar(
            navigationIcon = {
                InventraCloseButton(onClick = onCloseClick)
            },
            title = {
                Text(
                    "Gerenciar Membros",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
            },
        )
        when {
            uiState.isLoading -> CenteredLoading()
            uiState.errorMessage != null -> ErrorState(message = uiState.errorMessage, onRetryClick = onRetryClick)
            else ->
                MembersContent(
                    uiState = uiState,
                    onAcceptClick = onAcceptClick,
                    onRejectClick = onRejectClick,
                )
        }
    }
}

@Composable
private fun MembersContent(
    uiState: ManageMembersUiState,
    onAcceptClick: (PendingMemberRequest) -> Unit,
    onRejectClick: (PendingMemberRequest) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        uiState.actionErrorMessage?.let { message ->
            Text(text = message, color = InventraDanger, fontFamily = Montserrat, fontSize = 14.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle(text = "Solicitações pendentes")
            if (uiState.pendingRequests.isEmpty()) {
                EmptyText(text = "Nenhuma solicitação pendente.")
            }
            uiState.pendingRequests.forEach { request ->
                val isProcessing = uiState.processingRequestId == request.id
                InventraPendingMemberRow(
                    initials = request.initials,
                    name = request.name,
                    subtitle = if (isProcessing) "Processando..." else request.email,
                    onAcceptClick = { onAcceptClick(request) },
                    onRejectClick = { onRejectClick(request) },
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle(text = "Membros · ${uiState.members.size}")
            uiState.members.forEach { member ->
                InventraMemberRow(
                    initials = member.initials,
                    name = member.name,
                    roleLabel = member.roleLabel,
                    isOwner = member.isCurrentUser,
                    onRemoveClick = null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun CenteredLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = InventraPurple)
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, fontFamily = Montserrat, fontSize = 15.sp, textAlign = TextAlign.Center)
        InventraSecondaryButton(text = "Tentar novamente", onClick = onRetryClick)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = InventraPurple,
    )
}

@Composable
private fun EmptyText(text: String) {
    Text(text = text, fontFamily = Montserrat, fontSize = 14.sp, color = Color.Gray)
}

@Composable
fun ManageMembersRoute(
    navigator: Navigator,
    viewModel: ManageMembersViewModel = viewModel(factory = ManageMembersViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    ManageMembersScreen(
        uiState = uiState,
        onAcceptClick = viewModel::onAcceptClick,
        onRejectClick = viewModel::onRejectClick,
        onRejectReasonChange = viewModel::onRejectReasonChange,
        onRejectConfirm = viewModel::onRejectConfirm,
        onRejectDismiss = viewModel::onRejectDismiss,
        onRetryClick = viewModel::onRetryClick,
        onCloseClick = { navigator.back() },
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ManageMembersScreenPreview() {
    ManageMembersScreen(
        uiState =
            ManageMembersUiState(
                pendingRequests =
                    listOf(
                        PendingMemberRequest(
                            id = "1",
                            initials = "MR",
                            name = "Mauro Rosa",
                            email = "mauro.rosa@email.com",
                        ),
                    ),
                members =
                    listOf(
                        CozinhaMember(
                            id = "2",
                            initials = "JS",
                            name = "Jones Silva (você)",
                            roleLabel = "Supervisor",
                            isCurrentUser = true,
                        ),
                    ),
            ),
        onAcceptClick = {},
        onRejectClick = {},
        onRejectReasonChange = {},
        onRejectConfirm = {},
        onRejectDismiss = {},
        onRetryClick = {},
        onCloseClick = {},
    )
}
