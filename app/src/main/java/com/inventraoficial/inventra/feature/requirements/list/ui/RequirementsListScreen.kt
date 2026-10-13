package com.inventraoficial.inventra.feature.requirements.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.feature.requirements.RequisitionCard
import com.inventraoficial.inventra.feature.requirements.RequisitionSummary
import com.inventraoficial.inventra.feature.requirements.RequisitionTabs
import com.inventraoficial.inventra.feature.requirements.RequisitionsEmpty
import com.inventraoficial.inventra.feature.requirements.RequisitionsError
import com.inventraoficial.inventra.feature.requirements.RequisitionsLoading
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun RequirementsListScreen(
    requisitions: List<RequisitionSummary>,
    isSupervisor: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onRequisitionClick: (RequisitionSummary) -> Unit,
    onHistoryClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNewClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            title = {
                Text("Requisições", color = Color.White, fontFamily = Montserrat, fontSize = 17.sp)
            },
        )
        RequisitionTabs(selectedIndex = 0, onSelect = { index -> if (index == 1) onHistoryClick() })
        when {
            isLoading -> RequisitionsLoading(modifier = Modifier.weight(1f))
            errorMessage != null ->
                RequisitionsError(message = errorMessage, onRetryClick = onRetryClick, modifier = Modifier.weight(1f))
            requisitions.isEmpty() ->
                RequisitionsEmpty(
                    text =
                        if (isSupervisor) {
                            "Nenhuma requisição aguardando sua análise."
                        } else {
                            "Você não tem requisições abertas."
                        },
                    modifier = Modifier.weight(1f),
                )
            else ->
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(requisitions, key = { it.id }) { requisition ->
                        RequisitionCard(requisition = requisition, onClick = { onRequisitionClick(requisition) })
                    }
                }
        }
        InventraPrimaryButton(
            text = "Nova requisição",
            onClick = onNewClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )
    }
}

@Composable
fun RequirementsListRoute(
    navigator: Navigator,
    viewModel: RequirementsListViewModel = viewModel(factory = RequirementsListViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onScreenVisible()
    }

    RequirementsListScreen(
        requisitions = uiState.requisitions,
        isSupervisor = uiState.isSupervisor,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onRequisitionClick = { requisition ->
            if (requisition.status == RequisitionStatus.DRAFT) {
                navigator.navigate(Screen.RequisitionEditor(requisition.id))
            } else {
                navigator.navigate(Screen.RequisitionDetail(requisition.id))
            }
        },
        onNewClick = { navigator.navigate(Screen.RequisitionEditor()) },
        onHistoryClick = { navigator.navigate(Screen.RequirementsHistory) },
        onRetryClick = viewModel::onRetryClick,
        modifier = modifier,
    )
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun RequirementsListScreenPreview() {
    RequirementsListScreen(
        requisitions =
            listOf(
                RequisitionSummary(
                    id = 12,
                    title = "Compra #12",
                    subtitle = "Maria Souza · 11/10/2026",
                    details = "Leite Integral, Arroz Camil e mais 2",
                    status = RequisitionStatus.UNDER_REVIEW,
                ),
            ),
        isSupervisor = true,
        isLoading = false,
        errorMessage = null,
        onRequisitionClick = {},
        onHistoryClick = {},
        onRetryClick = {},
    )
}
