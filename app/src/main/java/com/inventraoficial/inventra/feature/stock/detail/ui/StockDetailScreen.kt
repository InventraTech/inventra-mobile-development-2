package com.inventraoficial.inventra.feature.stock.detail.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBatch
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBatchesCard
import com.inventraoficial.inventra.core.designsystem.organisms.InventraHistorySection
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTotalCard
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun StockDetailScreen(
    productImageRes: Int,
    productName: String,
    totalLabel: String,
    totalValue: String,
    totalCaption: String,
    batchesTitle: String,
    batches: List<InventraBatch>,
    onSeeAllBatchesClick: () -> Unit,
    supplier: String,
    storage: String,
    onEntriesClick: () -> Unit,
    onExitsClick: () -> Unit,
    onStockOutClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        InventraTopBar(
            navigationIcon = {
                InventraBackButton(onClick = onBackClick)
            },
            title = {
                Text(
                    productName,
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontSize = 17.sp,
                )
            },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Image(
                painter = painterResource(id = productImageRes),
                contentDescription = productName,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
            )
            InventraTotalCard(
                label = totalLabel,
                value = totalValue,
                caption = totalCaption,
            )
            InventraBatchesCard(
                title = batchesTitle,
                batches = batches,
                onSeeAll = onSeeAllBatchesClick,
            )
            InventraLabeledField(
                label = "Fornecedor",
                value = supplier,
                onValueChange = {},
                readOnly = true,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraLabeledField(
                label = "Armazenamento",
                value = storage,
                onValueChange = {},
                readOnly = true,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraHistorySection(
                onEntriesClick = onEntriesClick,
                onExitsClick = onExitsClick,
            )
            InventraPrimaryButton(
                text = "Dar Baixa",
                onClick = onStockOutClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun StockDetailRoute(
    viewModel: StockDetailViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    StockDetailScreen(
        productImageRes = uiState.productImageRes,
        productName = uiState.productName,
        totalLabel = uiState.totalLabel,
        totalValue = uiState.totalValue,
        totalCaption = uiState.totalCaption,
        batchesTitle = uiState.batchesTitle,
        batches = uiState.batches,
        onSeeAllBatchesClick = viewModel::onSeeAllBatchesClick,
        supplier = uiState.supplier,
        storage = uiState.storage,
        onEntriesClick = viewModel::onEntriesClick,
        onExitsClick = viewModel::onExitsClick,
        onStockOutClick = viewModel::onStockOutClick,
        onBackClick = viewModel::onBackClick,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun StockDetailScreenPreview() {
    StockDetailScreen(
        productImageRes = R.drawable.ic_inventra_logo,
        productName = "Leite Integral",
        totalLabel = "Quantidade total",
        totalValue = "18 L",
        totalCaption = "Em 3 lotes de 6 litros.",
        batchesTitle = "Lotes próximos da validade",
        batches =
            listOf(
                InventraBatch(dateLabel = "AMANHÃ", code = "000067", quantity = "6 L", isUrgent = true),
                InventraBatch(dateLabel = "06/07/2026", code = "000069", quantity = "6 L"),
            ),
        onSeeAllBatchesClick = {},
        supplier = "Seara Alimentos",
        storage = "Câmara fria 02",
        onEntriesClick = {},
        onExitsClick = {},
        onStockOutClick = {},
        onBackClick = {},
    )
}
