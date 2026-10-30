package com.inventraoficial.inventra.feature.stock.edit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.core.designsystem.molecules.InventraConfirmDialog
import com.inventraoficial.inventra.core.designsystem.organisms.InventraPickerOption
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScreenContent
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.feature.stock.productform.ProductFormActions
import com.inventraoficial.inventra.feature.stock.productform.ProductFormFields
import com.inventraoficial.inventra.feature.stock.productform.ProductFormState
import com.inventraoficial.inventra.feature.stock.productform.ProductPhotoField
import com.inventraoficial.inventra.feature.stock.productform.ProductPhotoOptions
import com.inventraoficial.inventra.feature.stock.productform.ProductPhotoState
import com.inventraoficial.inventra.feature.stock.productform.productPhotoActions
import com.inventraoficial.inventra.feature.suppliers.create.PickedSupplierEffect
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun ProductEditScreen(
    uiState: ProductEditUiState,
    actions: ProductEditActions,
    modifier: Modifier = Modifier,
    deactivate: ProductDeactivateUiState = ProductDeactivateUiState(),
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            navigationIcon = { InventraBackButton(onClick = actions.onBackClick) },
            title = { Text("Editar produto", color = Color.White, fontFamily = Montserrat, fontSize = 17.sp) },
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
                        modifier = contentModifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            uiState.errorMessage,
                            fontFamily = Montserrat,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                        )
                        InventraSecondaryButton(text = "Tentar novamente", onClick = actions.onRetryClick)
                    }
                else ->
                    Column(
                        modifier =
                            contentModifier
                                .imePadding()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        ProductPhotoField(photo = uiState.photo, onClick = actions.photo.onPhotoClick)
                        ProductFormFields(form = uiState.form, actions = actions.form)
                        uiState.submitError?.let {
                            Text(it, fontFamily = Montserrat, fontSize = 14.sp, color = InventraDanger)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InventraSecondaryButton(
                                text = "Cancelar",
                                onClick = actions.onBackClick,
                                modifier = Modifier.weight(1f),
                            )
                            InventraPrimaryButton(
                                text = "Salvar",
                                onClick = actions.onSaveClick,
                                isLoading = uiState.isSubmitting,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    DeactivateSection(state = deactivate, onClick = actions.deactivate.onDeactivateClick)
                    }
            }
        }
    }

    ProductPhotoOptions(photo = uiState.photo, actions = actions.photo)
    if (deactivate.isConfirmVisible) {
        InventraConfirmDialog(
            title = "Desativar ${uiState.form.name}?",
            message =
                "O produto sai do estoque e do catálogo de todas as cozinhas. " +
                    "O histórico de entradas e saídas continua guardado.",
            confirmText = "Desativar",
            onConfirm = actions.deactivate.onConfirm,
            onDismiss = actions.deactivate.onDismiss,
            isDestructive = true,
        )
    }
}

@Composable
private fun DeactivateSection(
    state: ProductDeactivateUiState,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.isDeactivating) {
            CircularProgressIndicator(color = InventraDanger)
        } else {
            InventraTextLink(text = "Desativar produto", onClick = onClick, color = InventraDanger)
        }
        state.errorMessage?.let {
            Text(it, fontFamily = Montserrat, fontSize = 14.sp, color = InventraDanger)
        }
    }
}

@Composable
fun ProductEditRoute(
    productId: Int,
    navigator: Navigator,
    viewModel: ProductEditViewModel =
        viewModel(key = "product-edit-$productId", factory = ProductEditViewModel.factory(productId)),
    modifier: Modifier = Modifier,
    deactivateViewModel: ProductDeactivateViewModel =
        viewModel(key = "product-deactivate-$productId", factory = ProductDeactivateViewModel.factory(productId)),
) {
    val uiState by viewModel.uiState.collectAsState()
    val deactivate by deactivateViewModel.uiState.collectAsState()

    LaunchedEffect(deactivate.isDeactivated) {
        if (deactivate.isDeactivated) {
            navigator.back()
            if (navigator.currentBackStack.lastOrNull() == Screen.StockDetail(productId)) navigator.back()
        }
    }

    val photoActions =
        productPhotoActions(
            onPhotoClick = viewModel::onPhotoClick,
            onSheetDismiss = viewModel::onPhotoSheetDismiss,
            onPhotoPicked = viewModel::onPhotoPicked,
            onCameraDenied = viewModel::onCameraDenied,
            onRemoveClick = viewModel::onRemovePhotoClick,
        )

    PickedSupplierEffect { viewModel.formActions.onSupplierCreated(it.id, it.name) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) navigator.back()
    }

    ProductEditScreen(
        uiState = uiState,
        actions =
            ProductEditActions(
                form = viewModel.formActions.copy(onNewSupplierClick = { navigator.navigate(Screen.SupplierCreate) }),
                photo = photoActions,
                onSaveClick = viewModel::onSaveClick,
                onRetryClick = viewModel::onRetryClick,
                onBackClick = { navigator.back() },
                deactivate =
                    ProductDeactivateActions(
                        onDeactivateClick = deactivateViewModel::onDeactivateClick,
                        onConfirm = deactivateViewModel::onConfirm,
                        onDismiss = deactivateViewModel::onDismiss,
                    ),
            ),
        modifier = modifier,
        deactivate = deactivate,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ProductEditScreenPreview() {
    ProductEditScreen(
        uiState =
            ProductEditUiState(
                isLoading = false,
                form =
                    ProductFormState(
                        name = "Ketchup",
                        brand = "Heinz",
                        barcode = "7896102000122",
                        units = listOf(InventraPickerOption(id = 6, title = "Unidade", subtitle = "UN")),
                        unitId = 6,
                        minStock = "4",
                        suppliers = listOf(InventraPickerOption(id = 2, title = "Atacadão")),
                        supplierId = 2,
                        referencePrice = "12,50",
                    ),
                photo = ProductPhotoState(isUploading = true),
            ),
        actions =
            ProductEditActions(
                form = ProductFormActions(),
                onSaveClick = {},
                onRetryClick = {},
                onBackClick = {},
            ),
    )
}
