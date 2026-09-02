package com.inventraoficial.inventra.feature.stock.stockout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraQuantityStepper
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSectionLabel
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun StockOutScreen(
    productName: String,
    batchCode: String,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    reason: String,
    onReasonChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit,
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
                    "Dar Baixa",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontSize = 17.sp,
                )
            },
            actions = {
                Text(
                    "Cancelar",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onCancelClick),
                )
            },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            InventraLabeledField(
                label = "Produto",
                value = productName,
                onValueChange = {},
                readOnly = true,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraLabeledField(
                label = "Lote",
                value = batchCode,
                onValueChange = {},
                readOnly = true,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                InventraSectionLabel(text = "Quantidade")
                InventraQuantityStepper(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            InventraLabeledField(
                label = "Motivo",
                value = reason,
                onValueChange = onReasonChange,
                multiline = true,
                placeholder = "Descreva o motivo da baixa",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraPrimaryButton(
                text = "Confirmar",
                onClick = onConfirmClick,
                modifier = Modifier.fillMaxWidth(),
            )
            InventraSecondaryButton(
                text = "Cancelar",
                onClick = onCancelClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun StockOutRoute(
    viewModel: StockOutViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    StockOutScreen(
        productName = uiState.productName,
        batchCode = uiState.batchCode,
        quantity = uiState.quantity,
        onQuantityChange = viewModel::onQuantityChange,
        reason = uiState.reason,
        onReasonChange = viewModel::onReasonChange,
        onConfirmClick = viewModel::onConfirmClick,
        onCancelClick = viewModel::onCancelClick,
        onBackClick = viewModel::onBackClick,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun StockOutScreenPreview() {
    StockOutScreen(
        productName = "Arroz Camil",
        batchCode = "000067",
        quantity = 0,
        onQuantityChange = {},
        reason = "",
        onReasonChange = {},
        onConfirmClick = {},
        onCancelClick = {},
        onBackClick = {},
    )
}
