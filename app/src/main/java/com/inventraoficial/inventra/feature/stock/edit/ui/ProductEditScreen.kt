package com.inventraoficial.inventra.feature.stock.edit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun ProductEditScreen(
    productName: String,
    onProductNameChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    supplier: String,
    onSupplierChange: (String) -> Unit,
    storage: String,
    onStorageChange: (String) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    onSaveClick: () -> Unit,
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
                    "Editar Produto",
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            InventraLabeledField(
                label = "Nome do produto",
                value = productName,
                onValueChange = onProductNameChange,
                placeholder = "Insira o nome do produto",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraLabeledField(
                label = "Categoria",
                value = category,
                onValueChange = onCategoryChange,
                placeholder = "Insira a categoria",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraLabeledField(
                label = "Fornecedor",
                value = supplier,
                onValueChange = onSupplierChange,
                placeholder = "Insira o fornecedor",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraLabeledField(
                label = "Armazenamento",
                value = storage,
                onValueChange = onStorageChange,
                placeholder = "Insira o local de armazenamento",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraLabeledField(
                label = "Quantidade total",
                value = quantity,
                onValueChange = onQuantityChange,
                placeholder = "Insira a quantidade",
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                InventraSecondaryButton(
                    text = "Cancelar",
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f),
                )
                InventraPrimaryButton(
                    text = "Salvar",
                    onClick = onSaveClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun ProductEditRoute(
    navigator: Navigator,
    viewModel: ProductEditViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    ProductEditScreen(
        productName = uiState.productName,
        onProductNameChange = viewModel::onProductNameChange,
        category = uiState.category,
        onCategoryChange = viewModel::onCategoryChange,
        supplier = uiState.supplier,
        onSupplierChange = viewModel::onSupplierChange,
        storage = uiState.storage,
        onStorageChange = viewModel::onStorageChange,
        quantity = uiState.quantity,
        onQuantityChange = viewModel::onQuantityChange,
        onSaveClick = viewModel::onSaveClick,
        onCancelClick = { navigator.back() },
        onBackClick = { navigator.back() },
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ProductEditScreenPreview() {
    ProductEditScreen(
        productName = "Leite Integral",
        onProductNameChange = {},
        category = "Laticínios",
        onCategoryChange = {},
        supplier = "Seara Alimentos",
        onSupplierChange = {},
        storage = "Câmara fria 02",
        onStorageChange = {},
        quantity = "18 L",
        onQuantityChange = {},
        onSaveClick = {},
        onCancelClick = {},
        onBackClick = {},
    )
}
