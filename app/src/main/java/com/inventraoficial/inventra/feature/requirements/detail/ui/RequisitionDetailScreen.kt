package com.inventraoficial.inventra.feature.requirements.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraDangerButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraConfirmDialog
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScreenContent
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType
import com.inventraoficial.inventra.feature.requirements.RequisitionStatusChip
import com.inventraoficial.inventra.feature.requirements.RequisitionsError
import com.inventraoficial.inventra.feature.requirements.RequisitionsLoading
import com.inventraoficial.inventra.feature.requirements.label
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun RequisitionDetailScreen(
    uiState: RequisitionDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onCancelClick: () -> Unit,
    onReasonChange: (String) -> Unit,
    onDialogConfirm: () -> Unit,
    onDialogDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            navigationIcon = { InventraBackButton(onClick = onBackClick) },
            title = {
                Text(
                    "Requisição",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
            },
        )
        InventraScreenContent {
            when {
                uiState.isLoading -> RequisitionsLoading(modifier = Modifier.weight(1f))
                uiState.errorMessage != null ->
                    RequisitionsError(
                        message = uiState.errorMessage,
                        onRetryClick = onRetryClick,
                        modifier = Modifier.weight(1f),
                    )
                else ->
                    RequisitionDetailContent(
                        uiState = uiState,
                        onApproveClick = onApproveClick,
                        onRejectClick = onRejectClick,
                        onCancelClick = onCancelClick,
                        modifier = Modifier.weight(1f),
                    )
            }
        }
    }

    uiState.pendingAction?.let { action ->
        ActionDialog(
            action = action,
            type = uiState.type,
            restoresStock = uiState.restoresStockOnCancel,
            reasonText = uiState.reasonText,
            errorMessage = uiState.actionErrorMessage,
            onReasonChange = onReasonChange,
            onConfirm = onDialogConfirm,
            onDismiss = onDialogDismiss,
        )
    }
}

@Composable
private fun RequisitionDetailContent(
    uiState: RequisitionDetailUiState,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = uiState.title,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = InventraPurple,
                modifier = Modifier.weight(1f),
            )
            uiState.status?.let { RequisitionStatusChip(status = it) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoRow(label = "Solicitante", value = uiState.requesterName)
            InfoRow(label = "Data", value = uiState.dateText)
            uiState.origin?.let { InfoRow(label = "Origem", value = it) }
            uiState.reason?.let { InfoRow(label = "Motivo", value = it) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Itens",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = InventraPurple,
            )
            if (uiState.items.isEmpty()) {
                Text(
                    text = "Nenhum item nesta requisição.",
                    fontFamily = Montserrat,
                    fontSize = 14.sp,
                    color = InventraMuted,
                )
            }
            uiState.items.forEach { ItemCard(item = it) }
            uiState.totalText?.let {
                InfoRow(label = "Total estimado", value = it)
            }
        }
        uiState.actionErrorMessage?.takeIf { uiState.pendingAction == null }?.let {
            Text(text = it, fontFamily = Montserrat, fontSize = 14.sp, color = InventraDanger)
        }
        if (uiState.canReview) {
            InventraPrimaryButton(
                text = "Aprovar",
                onClick = onApproveClick,
                isLoading = uiState.isProcessing,
                modifier = Modifier.fillMaxWidth(),
            )
            InventraDangerButton(text = "Recusar", onClick = onRejectClick, modifier = Modifier.fillMaxWidth())
        }
        if (uiState.canCancel) {
            InventraDangerButton(
                text = "Cancelar requisição",
                onClick = onCancelClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Row {
        Text(
            text = label,
            fontFamily = Montserrat,
            fontSize = 14.sp,
            color = InventraMuted,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun ItemCard(item: RequisitionItemUi) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), shape)
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.name,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = InventraPurple,
                modifier = Modifier.weight(1f),
            )
            Text(text = item.quantityText, fontFamily = Montserrat, fontSize = 14.sp)
        }
        item.priceText?.let { Text(text = it, fontFamily = Montserrat, fontSize = 13.sp, color = InventraMuted) }
        item.supplier?.let {
            Text(text = "Fornecedor: $it", fontFamily = Montserrat, fontSize = 13.sp, color = InventraMuted)
        }
        item.note?.let { Text(text = it, fontFamily = Montserrat, fontSize = 13.sp) }
    }
}

@Composable
private fun ActionDialog(
    action: PendingAction,
    type: RequisitionType?,
    restoresStock: Boolean,
    reasonText: String,
    errorMessage: String?,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val (title, message, confirmText) =
        when (action) {
            PendingAction.Approve ->
                Triple(
                    "Aprovar requisição?",
                    if (type == RequisitionType.PURCHASE) {
                        "A entrada no estoque acontece quando a mercadoria chegar."
                    } else {
                        "Os itens vão sair do estoque da cozinha."
                    },
                    "Aprovar",
                )
            PendingAction.Reject -> Triple("Recusar requisição?", "Quem pediu vai ver o motivo.", "Recusar")
            PendingAction.Cancel ->
                Triple(
                    "Cancelar requisição?",
                    if (restoresStock) {
                        "O que a aprovação baixou volta para o estoque."
                    } else {
                        "Ela sai da análise do supervisor."
                    },
                    "Cancelar requisição",
                )
        }
    InventraConfirmDialog(
        title = title,
        message = message,
        confirmText = confirmText,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        dismissText = "Voltar",
        isDestructive = action != PendingAction.Approve,
    ) {
        if (action != PendingAction.Approve) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InventraLabeledField(
                    label = "Motivo",
                    value = reasonText,
                    onValueChange = onReasonChange,
                    placeholder = "Ex.: quantidade acima do necessário",
                    modifier = Modifier.fillMaxWidth(),
                )
                errorMessage?.let { Text(text = it, fontFamily = Montserrat, fontSize = 13.sp, color = InventraDanger) }
            }
        }
    }
}

@Composable
fun RequisitionDetailRoute(
    requisitionId: Int,
    navigator: Navigator,
    viewModel: RequisitionDetailViewModel =
        viewModel(key = "requisition-$requisitionId", factory = RequisitionDetailViewModel.factory(requisitionId)),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) navigator.back()
    }

    RequisitionDetailScreen(
        uiState = uiState,
        onBackClick = { navigator.back() },
        onRetryClick = viewModel::onRetryClick,
        onApproveClick = viewModel::onApproveClick,
        onRejectClick = viewModel::onRejectClick,
        onCancelClick = viewModel::onCancelClick,
        onReasonChange = viewModel::onReasonChange,
        onDialogConfirm = viewModel::onDialogConfirm,
        onDialogDismiss = viewModel::onDialogDismiss,
        modifier = modifier,
    )
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun RequisitionDetailScreenPreview() {
    RequisitionDetailScreen(
        uiState =
            RequisitionDetailUiState(
                title = "Compra #12",
                status = RequisitionStatus.UNDER_REVIEW,
                type = RequisitionType.PURCHASE,
                requesterName = "Maria Souza",
                dateText = "11/10/2026",
                origin = "Reposição semanal",
                items =
                    listOf(
                        RequisitionItemUi("Leite Integral", "Qtd. 24", "Preço estimado R$ 5,49", "Italac", null),
                        RequisitionItemUi("Arroz Camil", "Qtd. 10", null, null, "Pacote de 5 kg"),
                    ),
                totalText = "R$ 131,76",
                canReview = true,
            ),
        onBackClick = {},
        onRetryClick = {},
        onApproveClick = {},
        onRejectClick = {},
        onCancelClick = {},
        onReasonChange = {},
        onDialogConfirm = {},
        onDialogDismiss = {},
    )
}
