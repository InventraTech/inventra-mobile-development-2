package com.inventraoficial.inventra.feature.stock.stockout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraFilterChip
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSectionLabel
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun StockOutScreen(
    uiState: StockOutUiState,
    onQuantityChange: (String) -> Unit,
    onReasonSelect: (StockOutReason) -> Unit,
    onConfirmClick: () -> Unit,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
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
                InventraBackButton(onClick = onBackClick)
            },
            title = {
                Text(
                    "Dar Baixa",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontSize = 17.sp,
                )
            },
        )
        val contentModifier = Modifier.weight(1f).fillMaxWidth()
        when {
            uiState.isLoading ->
                Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = InventraPurple)
                }
            uiState.errorMessage != null ->
                Column(
                    modifier = contentModifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(uiState.errorMessage, fontFamily = Montserrat, fontSize = 15.sp, textAlign = TextAlign.Center)
                    InventraSecondaryButton(text = "Tentar novamente", onClick = onRetryClick)
                }
            uiState.consumedBatches != null ->
                StockOutDone(
                    productName = uiState.productName,
                    batches = uiState.consumedBatches,
                    onDoneClick = onBackClick,
                    modifier = contentModifier,
                )
            else ->
                StockOutForm(
                    uiState = uiState,
                    onQuantityChange = onQuantityChange,
                    onReasonSelect = onReasonSelect,
                    onConfirmClick = onConfirmClick,
                    modifier = contentModifier,
                )
        }
    }
}

@Composable
private fun StockOutForm(
    uiState: StockOutUiState,
    onQuantityChange: (String) -> Unit,
    onReasonSelect: (StockOutReason) -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                uiState.productName,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = InventraPurple,
            )
            uiState.details?.let { Text(it, fontFamily = Montserrat, fontSize = 13.sp, color = InventraMuted) }
            Text(
                "Saldo na cozinha: ${uiState.available} ${uiState.unitSymbol}",
                fontFamily = Montserrat,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            uiState.nextExpiration?.let {
                Text("Próxima validade: $it", fontFamily = Montserrat, fontSize = 13.sp, color = InventraMuted)
            }
        }
        InventraLabeledField(
            label = "Quantidade (${uiState.unitSymbol})",
            value = uiState.quantity,
            onValueChange = onQuantityChange,
            keyboardType = KeyboardType.Decimal,
            placeholder = "Ex.: 2,5",
            modifier = Modifier.fillMaxWidth(),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InventraSectionLabel(text = "Motivo")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StockOutReason.entries.forEach { reason ->
                    InventraFilterChip(
                        label = reason.label,
                        selected = reason == uiState.reason,
                        onClick = { onReasonSelect(reason) },
                    )
                }
            }
        }
        Text(
            if (uiState.hasStock) {
                "A baixa sai primeiro dos lotes que vencem antes."
            } else {
                "Este produto está sem saldo na cozinha."
            },
            fontFamily = Montserrat,
            fontSize = 13.sp,
            color = InventraMuted,
        )
        uiState.submitError?.let {
            Text(it, fontFamily = Montserrat, fontSize = 14.sp, color = InventraDanger)
        }
        InventraPrimaryButton(
            text = "Confirmar baixa",
            onClick = onConfirmClick,
            enabled = uiState.hasStock,
            isLoading = uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StockOutDone(
    productName: String,
    batches: List<ConsumedBatchLine>,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(96.dp)
                    .background(InventraSuccess.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = InventraSuccess,
                modifier = Modifier.size(48.dp),
            )
        }
        Text("Baixa registrada", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(productName, fontFamily = Montserrat, fontSize = 14.sp, color = InventraMuted)
        batches.forEach { batch ->
            Text(
                "Lote ${batch.batchNumber}: −${batch.consumed} (ficou ${batch.remaining})",
                fontFamily = Montserrat,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
        InventraPrimaryButton(
            text = "Concluir",
            onClick = onDoneClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
        )
    }
}

@Composable
fun StockOutRoute(
    productId: Int,
    navigator: Navigator,
    viewModel: StockOutViewModel =
        viewModel(key = "stock-out-$productId", factory = StockOutViewModel.factory(productId)),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    StockOutScreen(
        uiState = uiState,
        onQuantityChange = viewModel::onQuantityChange,
        onReasonSelect = viewModel::onReasonSelect,
        onConfirmClick = viewModel::onConfirmClick,
        onRetryClick = viewModel::onRetryClick,
        onBackClick = { navigator.back() },
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun StockOutScreenPreview() {
    StockOutScreen(
        uiState =
            StockOutUiState(
                isLoading = false,
                productName = "Leite Integral",
                details = "Italac · Laticínios",
                unitSymbol = "L",
                available = "12,5",
                nextExpiration = "20/10/2026",
                hasStock = true,
            ),
        onQuantityChange = {},
        onReasonSelect = {},
        onConfirmClick = {},
        onRetryClick = {},
        onBackClick = {},
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun StockOutDonePreview() {
    StockOutScreen(
        uiState =
            StockOutUiState(
                isLoading = false,
                productName = "Leite Integral",
                consumedBatches =
                    listOf(
                        ConsumedBatchLine("L2026-001", "6 L", "0 L"),
                        ConsumedBatchLine("L2026-002", "2 L", "4 L"),
                    ),
            ),
        onQuantityChange = {},
        onReasonSelect = {},
        onConfirmClick = {},
        onRetryClick = {},
        onBackClick = {},
    )
}
