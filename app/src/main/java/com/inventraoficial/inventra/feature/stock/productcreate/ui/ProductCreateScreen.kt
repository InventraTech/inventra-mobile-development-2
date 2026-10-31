package com.inventraoficial.inventra.feature.stock.productcreate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.core.designsystem.organisms.InventraPickerOption
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScreenContent
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.feature.stock.productform.ProductFormActions
import com.inventraoficial.inventra.feature.stock.productform.ProductFormFields
import com.inventraoficial.inventra.feature.stock.productform.ProductFormState
import com.inventraoficial.inventra.feature.stock.productform.ProductPhotoField
import com.inventraoficial.inventra.feature.stock.productform.ProductPhotoOptions
import com.inventraoficial.inventra.feature.stock.productform.productPhotoActions
import com.inventraoficial.inventra.feature.suppliers.create.PickedSupplierEffect
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun ProductCreateScreen(
    uiState: ProductCreateUiState,
    actions: ProductCreateActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            navigationIcon = { InventraBackButton(onClick = actions.onBackClick) },
            title = { Text("Novo produto", color = Color.White, fontFamily = Montserrat, fontSize = 17.sp) },
        )
        InventraScreenContent {
            val contentModifier = Modifier.weight(1f).fillMaxWidth()
            val created = uiState.createdProduct
            val existing = uiState.existingProduct
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
                created != null ->
                    ProductResult(
                        icon = Icons.Default.CheckCircle,
                        iconTint = InventraSuccess,
                        title = "Produto cadastrado",
                        text = uiState.createdText(created.name),
                        product = created,
                        actions = actions,
                        modifier = contentModifier,
                    )
                existing != null ->
                    ProductResult(
                        icon = Icons.Default.Inventory2,
                        iconTint = InventraPurple,
                        title = "Já está no catálogo",
                        text = "Esse código já foi cadastrado como ${existing.name}.",
                        product = existing,
                        actions = actions,
                        modifier = contentModifier,
                    )
                else -> ProductForm(uiState = uiState, actions = actions, modifier = contentModifier)
            }
        }
    }
    ProductPhotoOptions(photo = uiState.photo, actions = actions.photo)
}

@Composable
private fun ProductForm(
    uiState: ProductCreateUiState,
    actions: ProductCreateActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (uiState.isPrefilled) {
            Text(
                "Preenchemos com o que encontramos pelo código de barras. Confira antes de salvar.",
                fontFamily = Montserrat,
                fontSize = 13.sp,
                color = InventraPurple,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(InventraPurple.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
            )
        }
        ProductPhotoField(photo = uiState.photo, onClick = actions.photo.onPhotoClick)
        ProductFormFields(form = uiState.form, actions = actions.form)
        uiState.submitError?.let {
            Text(it, fontFamily = Montserrat, fontSize = 14.sp, color = InventraDanger)
        }
        InventraPrimaryButton(
            text = "Cadastrar produto",
            onClick = actions.onSaveClick,
            isLoading = uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun ProductCreateUiState.createdText(name: String): String {
    val stock =
        if (kitchenLinkFailed) {
            "$name já está no catálogo, mas só aparece no estoque da cozinha depois da primeira entrada."
        } else {
            "$name já está no catálogo e na lista do estoque."
        }
    val photo = if (photoUploadFailed) " A foto não foi enviada; dá para colocar pelo Editar produto." else ""
    return "$stock$photo Quer dar entrada agora?"
}

// Fim do fluxo: produto recém-cadastrado ou código que já estava no catálogo.
@Composable
private fun ProductResult(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    text: String,
    product: SavedProduct,
    actions: ProductCreateActions,
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
                    .background(iconTint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(48.dp))
        }
        Text(title, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(text, fontFamily = Montserrat, fontSize = 14.sp, color = InventraMuted, textAlign = TextAlign.Center)
        InventraPrimaryButton(
            text = "Dar entrada",
            onClick = { actions.onStockEntryClick(product.id) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
        )
        InventraSecondaryButton(
            text = "Ver produto",
            onClick = { actions.onSeeProductClick(product.id) },
            modifier = Modifier.fillMaxWidth(),
        )
        InventraTextLink(text = "Concluir", onClick = actions.onBackClick)
    }
}

@Composable
fun ProductCreateRoute(
    barcode: String?,
    navigator: Navigator,
    viewModel: ProductCreateViewModel =
        viewModel(key = "product-create-$barcode", factory = ProductCreateViewModel.factory(barcode)),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val photoActions =
        productPhotoActions(
            onPhotoClick = viewModel::onPhotoClick,
            onSheetDismiss = viewModel::onPhotoSheetDismiss,
            onPhotoPicked = viewModel::onPhotoPicked,
            onCameraDenied = viewModel::onCameraDenied,
            onRemoveClick = viewModel::onRemovePhotoClick,
        )
    PickedSupplierEffect { viewModel.formActions.onSupplierCreated(it.id, it.name) }

    // Dar entrada e Ver produto tomam o lugar do cadastro: voltar dali leva para onde o cadastro começou.
    fun replaceWith(screen: Screen) {
        navigator.back()
        navigator.navigate(screen)
    }

    ProductCreateScreen(
        uiState = uiState,
        actions =
            ProductCreateActions(
                form = viewModel.formActions.copy(onNewSupplierClick = { navigator.navigate(Screen.SupplierCreate) }),
                photo = photoActions,
                onSaveClick = viewModel::onSaveClick,
                onRetryClick = viewModel::onRetryClick,
                onBackClick = { navigator.back() },
                onStockEntryClick = { productId -> replaceWith(Screen.StockEntry(productId)) },
                onSeeProductClick = { productId -> replaceWith(Screen.StockDetail(productId)) },
            ),
        modifier = modifier,
    )
}

private val previewActions =
    ProductCreateActions(
        form = ProductFormActions(),
        onSaveClick = {},
        onRetryClick = {},
        onBackClick = {},
        onStockEntryClick = {},
        onSeeProductClick = {},
    )

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ProductCreateFormPreview() {
    ProductCreateScreen(
        uiState =
            ProductCreateUiState(
                isLoading = false,
                isPrefilled = true,
                form =
                    ProductFormState(
                        name = "Coca-Cola",
                        brand = "Coca-Cola",
                        barcode = "7894900011517",
                        units = listOf(InventraPickerOption(1, "Litro", "L")),
                        unitId = 1,
                        categories = listOf(InventraPickerOption(2, "Mercearia")),
                        suppliers = listOf(InventraPickerOption(id = 3, title = "Solar Coca-Cola")),
                        supplierId = 3,
                        referencePrice = "9,90",
                    ),
            ),
        actions = previewActions,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ProductCreateDonePreview() {
    ProductCreateScreen(
        uiState = ProductCreateUiState(isLoading = false, createdProduct = SavedProduct(1, "Coca-Cola 2L")),
        actions = previewActions,
    )
}
