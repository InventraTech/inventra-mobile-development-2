package com.inventraoficial.inventra.feature.stock.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraProductImage
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraDeliveryHistoryRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBatch
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBatchesCard
import com.inventraoficial.inventra.core.designsystem.organisms.InventraHistorySection
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScreenContent
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTotalCard
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun StockDetailScreen(
    uiState: StockDetailUiState,
    actions: StockDetailActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            navigationIcon = {
                InventraBackButton(onClick = actions.onBackClick)
            },
            title = {
                Text(
                    uiState.productName,
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontSize = 17.sp,
                )
            },
            actions = {
                if (uiState.canEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar produto",
                        tint = Color.White,
                        modifier = Modifier.clickable(onClick = actions.onEditClick),
                    )
                }
            },
        )
        InventraScreenContent {
            when {
                uiState.isLoading ->
                    CenteredBox(modifier = Modifier.weight(1f)) { CircularProgressIndicator(color = InventraPurple) }
                uiState.errorMessage != null && uiState.productName.isEmpty() ->
                    ErrorState(uiState.errorMessage, actions.onRetryClick, Modifier.weight(1f))
                else -> DetailContent(uiState, actions, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DetailContent(
    uiState: StockDetailUiState,
    actions: StockDetailActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        InventraProductImage(
            photoUrl = uiState.productPhotoUrl,
            shape = RoundedCornerShape(16.dp),
            iconSize = 56.dp,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(180.dp),
        )
        uiState.productDetails?.let {
            Text(text = it, fontFamily = Montserrat, fontSize = 14.sp, color = InventraMuted)
        }
        InventraTotalCard(
            label = "Quantidade total",
            value = uiState.totalValue,
            caption = uiState.totalCaption,
        )
        if (uiState.batches.isNotEmpty()) {
            InventraBatchesCard(
                title = "Lotes por validade",
                batches = uiState.batches,
                onSeeAll = actions.onSeeAllBatchesClick,
                seeAllText = uiState.seeAllBatchesText,
            )
        }
        InventraLabeledField(
            label = "Fornecedor",
            value = uiState.supplier,
            onValueChange = {},
            readOnly = true,
            placeholder = "",
            modifier = Modifier.fillMaxWidth(),
        )
        InventraHistorySection(
            onEntriesClick = actions.onEntriesClick,
            onExitsClick = actions.onExitsClick,
        )
        uiState.history?.let { HistoryList(it) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InventraPrimaryButton(
                text = "Dar Entrada",
                onClick = actions.onStockEntryClick,
                modifier = Modifier.weight(1f),
            )
            InventraPrimaryButton(
                text = "Dar Baixa",
                onClick = actions.onStockOutClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HistoryList(history: HistoryState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = history.type.title,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = InventraInk,
        )
        when {
            history.isLoading ->
                CenteredBox(modifier = Modifier.fillMaxWidth()) { CircularProgressIndicator(color = InventraPurple) }
            history.errorMessage != null ->
                Text(text = history.errorMessage, fontFamily = Montserrat, fontSize = 14.sp, color = InventraDanger)
            history.entries.isEmpty() ->
                Text(text = history.type.emptyText, fontFamily = Montserrat, fontSize = 14.sp, color = InventraMuted)
            else ->
                history.entries.forEach { entry ->
                    InventraDeliveryHistoryRow(
                        date = entry.date,
                        productName = entry.title,
                        quantityLabel = entry.quantityLabel,
                    )
                }
        }
    }
}

@Composable
private fun CenteredBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        content()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            fontFamily = Montserrat,
            fontSize = 15.sp,
            color = InventraInk,
            textAlign = TextAlign.Center,
        )
        InventraSecondaryButton(text = "Tentar novamente", onClick = onRetryClick)
    }
}

@Composable
fun StockDetailRoute(
    productId: Int,
    navigator: Navigator,
    viewModel: StockDetailViewModel =
        viewModel(key = "stock-detail-$productId", factory = StockDetailViewModel.factory(productId)),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onScreenVisible()
    }

    StockDetailScreen(
        uiState = uiState,
        actions =
            StockDetailActions(
                onSeeAllBatchesClick = viewModel::onSeeAllBatchesClick,
                onEntriesClick = viewModel::onEntriesClick,
                onExitsClick = viewModel::onExitsClick,
                onStockEntryClick = { navigator.navigate(Screen.StockEntry(productId)) },
                onStockOutClick = { navigator.navigate(Screen.StockOut(productId)) },
                onEditClick = { navigator.navigate(Screen.ProductEdit(productId)) },
                onRetryClick = viewModel::onRetryClick,
                onBackClick = { navigator.back() },
            ),
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview(heightDp = 1200)
@Composable
private fun StockDetailScreenPreview() {
    StockDetailScreen(
        uiState =
            StockDetailUiState(
                productPhotoUrl = null,
                productName = "Leite Integral",
                canEdit = true,
                productDetails = "Italac · Laticínios",
                totalValue = "18 L",
                totalCaption = "Em 3 lotes dentro da validade.",
                batches =
                    listOf(
                        InventraBatch(dateLabel = "AMANHÃ", code = "QA-01", quantity = "6 L", isUrgent = true),
                        InventraBatch(dateLabel = "06/11/2026", code = "QA-02", quantity = "6 L"),
                    ),
                seeAllBatchesText = "Ver todos os 4 lotes",
                supplier = "Seara Alimentos",
                history =
                    HistoryState(
                        type = HistoryType.Exits,
                        entries = listOf(HistoryEntry("12/10/2026 14:30", "Mauro Rosa · Uso", "−6 L · lote QA-01")),
                    ),
            ),
        actions = StockDetailActions(),
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun StockDetailScreenErrorPreview() {
    StockDetailScreen(
        uiState = StockDetailUiState(errorMessage = "Não foi possível conectar ao servidor. Verifique sua conexão."),
        actions = StockDetailActions(),
    )
}
