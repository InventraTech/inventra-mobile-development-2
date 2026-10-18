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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPickerField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.core.designsystem.organisms.InventraPickerOption
import com.inventraoficial.inventra.core.designsystem.organisms.InventraSearchPickerSheet
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
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
                    Text(uiState.errorMessage, fontFamily = Montserrat, fontSize = 15.sp, textAlign = TextAlign.Center)
                    InventraSecondaryButton(text = "Tentar novamente", onClick = actions.onRetryClick)
                }
            created != null ->
                ProductResult(
                    icon = Icons.Default.CheckCircle,
                    iconTint = InventraSuccess,
                    title = "Produto cadastrado",
                    text = "${created.name} já está no catálogo. Quer dar entrada do estoque agora?",
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

private enum class OpenPicker { Unit, Category }

@Composable
private fun ProductForm(
    uiState: ProductCreateUiState,
    actions: ProductCreateActions,
    modifier: Modifier = Modifier,
) {
    var openPicker by remember { mutableStateOf<OpenPicker?>(null) }

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
        InventraLabeledField(
            label = "Nome do produto",
            value = uiState.name,
            onValueChange = actions.onNameChange,
            placeholder = "Ex.: Arroz branco 5kg",
            modifier = Modifier.fillMaxWidth(),
        )
        InventraLabeledField(
            label = "Marca (opcional)",
            value = uiState.brand,
            onValueChange = actions.onBrandChange,
            placeholder = "Ex.: Tio João",
            modifier = Modifier.fillMaxWidth(),
        )
        InventraPickerField(
            label = "Unidade de medida",
            value = uiState.unitLabel,
            placeholder = "Escolha a unidade",
            onClick = { openPicker = OpenPicker.Unit },
            modifier = Modifier.fillMaxWidth(),
        )
        if (uiState.categories.isNotEmpty()) {
            InventraPickerField(
                label = "Categoria (opcional)",
                value = uiState.categoryName,
                placeholder = "Escolha a categoria",
                onClick = { openPicker = OpenPicker.Category },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        InventraLabeledField(
            label = "Código de barras (opcional)",
            value = uiState.barcode,
            onValueChange = actions.onBarcodeChange,
            keyboardType = KeyboardType.Number,
            placeholder = "Só os números",
            modifier = Modifier.fillMaxWidth(),
        )
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

    when (openPicker) {
        OpenPicker.Unit ->
            InventraSearchPickerSheet(
                title = "Unidade de medida",
                options = uiState.units,
                selectedId = uiState.unitId,
                onSelect = actions.onUnitSelect,
                onDismiss = { openPicker = null },
            )
        OpenPicker.Category ->
            InventraSearchPickerSheet(
                title = "Categoria",
                options = uiState.categories,
                selectedId = uiState.categoryId,
                onSelect = actions.onCategorySelect,
                onDismiss = { openPicker = null },
                noneLabel = "Sem categoria",
            )
        null -> Unit
    }
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

    // Dar entrada e Ver produto tomam o lugar do cadastro: voltar dali leva para onde o cadastro começou.
    fun replaceWith(screen: Screen) {
        navigator.back()
        navigator.navigate(screen)
    }

    ProductCreateScreen(
        uiState = uiState,
        actions =
            ProductCreateActions(
                onNameChange = viewModel::onNameChange,
                onBrandChange = viewModel::onBrandChange,
                onBarcodeChange = viewModel::onBarcodeChange,
                onUnitSelect = viewModel::onUnitSelect,
                onCategorySelect = viewModel::onCategorySelect,
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
        onNameChange = {},
        onBrandChange = {},
        onBarcodeChange = {},
        onUnitSelect = {},
        onCategorySelect = {},
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
                name = "Coca-Cola",
                brand = "Coca-Cola",
                barcode = "7894900011517",
                units = listOf(InventraPickerOption(1, "Litro", "L")),
                unitId = 1,
                categories = listOf(InventraPickerOption(2, "Mercearia")),
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
