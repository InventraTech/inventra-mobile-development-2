package com.inventraoficial.inventra.feature.requirements.history.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.inventraoficial.inventra.core.designsystem.organisms.InventraFilterChipRow
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
fun RequirementsHistoryScreen(
    requisitions: List<RequisitionSummary>,
    selectedFilter: HistoryFilter,
    isLoading: Boolean,
    errorMessage: String?,
    onFilterSelect: (HistoryFilter) -> Unit,
    onRequisitionClick: (RequisitionSummary) -> Unit,
    onRequirementsClick: () -> Unit,
    onRetryClick: () -> Unit,
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
                Text("Requisições", color = Color.White, fontFamily = Montserrat, fontSize = 17.sp)
            },
        )
        RequisitionTabs(selectedIndex = 1, onSelect = { index -> if (index == 0) onRequirementsClick() })
        InventraFilterChipRow(
            options = HistoryFilter.entries.map { it.label },
            selected = selectedFilter.label,
            onSelect = { label -> HistoryFilter.entries.firstOrNull { it.label == label }?.let(onFilterSelect) },
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        )
        when {
            isLoading -> RequisitionsLoading(modifier = Modifier.weight(1f))
            errorMessage != null ->
                RequisitionsError(message = errorMessage, onRetryClick = onRetryClick, modifier = Modifier.weight(1f))
            requisitions.isEmpty() ->
                RequisitionsEmpty(text = "Nenhuma requisição por aqui ainda.", modifier = Modifier.weight(1f))
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
    }
}

@Composable
fun RequirementsHistoryRoute(
    navigator: Navigator,
    viewModel: RequirementsHistoryViewModel = viewModel(factory = RequirementsHistoryViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onScreenVisible()
    }

    RequirementsHistoryScreen(
        requisitions = uiState.requisitions,
        selectedFilter = uiState.selectedFilter,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onFilterSelect = viewModel::onFilterSelect,
        onRequisitionClick = { requisition -> navigator.navigate(Screen.RequisitionDetail(requisition.id)) },
        onRequirementsClick = { navigator.back() },
        onRetryClick = viewModel::onRetryClick,
        modifier = modifier,
    )
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun RequirementsHistoryScreenPreview() {
    RequirementsHistoryScreen(
        requisitions =
            listOf(
                RequisitionSummary(
                    id = 9,
                    title = "Consumo #9",
                    subtitle = "Maria Souza · 08/10/2026",
                    details = "Motivo: quantidade acima do necessário",
                    status = RequisitionStatus.REJECTED,
                ),
            ),
        selectedFilter = HistoryFilter.All,
        isLoading = false,
        errorMessage = null,
        onFilterSelect = {},
        onRequisitionClick = {},
        onRequirementsClick = {},
        onRetryClick = {},
    )
}
