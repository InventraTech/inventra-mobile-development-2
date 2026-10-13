package com.inventraoficial.inventra.feature.requirements.editor.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraFilterChip
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.organisms.InventraChipGroup
import com.inventraoficial.inventra.core.designsystem.organisms.InventraFilterSection
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormSheet(
    form: ItemForm,
    type: RequisitionType,
    suppliers: List<SupplierOption>,
    isSaving: Boolean,
    actions: ItemFormActions,
) {
    ModalBottomSheet(
        onDismissRequest = actions.onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = InventraBackground,
    ) {
        ItemFormContent(form = form, type = type, suppliers = suppliers, isSaving = isSaving, actions = actions)
    }
}

@Composable
private fun ItemFormContent(
    form: ItemForm,
    type: RequisitionType,
    suppliers: List<SupplierOption>,
    isSaving: Boolean,
    actions: ItemFormActions,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Adicionar item",
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = InventraInk,
        )
        InventraLabeledField(
            label = "Produto",
            value = form.query,
            onValueChange = actions.onQueryChange,
            placeholder = "Buscar produto...",
            modifier = Modifier.fillMaxWidth(),
        )
        ProductResults(form = form, onProductSelect = actions.onProductSelect)
        InventraLabeledField(
            label = form.product?.let { "Quantidade (${it.unitSymbol})" } ?: "Quantidade",
            value = form.quantity,
            onValueChange = actions.onQuantityChange,
            keyboardType = KeyboardType.Decimal,
            placeholder = "Ex.: 20",
            modifier = Modifier.fillMaxWidth(),
        )
        InventraLabeledField(
            label = "Preço estimado por unidade (opcional)",
            value = form.estimatedPrice,
            onValueChange = actions.onPriceChange,
            keyboardType = KeyboardType.Decimal,
            placeholder = "Ex.: 7,00",
            modifier = Modifier.fillMaxWidth(),
        )
        if (type == RequisitionType.PURCHASE && suppliers.isNotEmpty()) {
            InventraFilterSection("Fornecedor sugerido (opcional)") {
                InventraChipGroup {
                    InventraFilterChip(
                        label = "Nenhum",
                        selected = form.supplierId == null,
                        onClick = { actions.onSupplierSelect(null) },
                    )
                    suppliers.forEach { supplier ->
                        InventraFilterChip(
                            label = supplier.name,
                            selected = form.supplierId == supplier.id,
                            onClick = { actions.onSupplierSelect(supplier.id) },
                        )
                    }
                }
            }
        }
        InventraLabeledField(
            label = "Observação (opcional)",
            value = form.note,
            onValueChange = actions.onNoteChange,
            placeholder = "Ex.: entregar até sexta",
            modifier = Modifier.fillMaxWidth(),
        )
        form.errorMessage?.let { Text(text = it, fontFamily = Montserrat, fontSize = 14.sp, color = InventraDanger) }
        InventraPrimaryButton(
            text = "Adicionar à requisição",
            onClick = actions.onConfirm,
            isLoading = isSaving,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ProductResults(
    form: ItemForm,
    onProductSelect: (ProductOption) -> Unit,
) {
    when {
        form.isSearching -> CircularProgressIndicator(color = InventraPurple, modifier = Modifier.size(24.dp))
        form.product != null ->
            Text(
                text = "Selecionado: ${form.product.name}",
                fontFamily = Montserrat,
                fontSize = 13.sp,
                color = InventraPurple,
            )
        form.results.isNotEmpty() ->
            Column {
                form.results.take(MAX_RESULTS).forEach { product ->
                    Text(
                        text = "${product.name} (${product.unitSymbol})",
                        fontFamily = Montserrat,
                        fontSize = 14.sp,
                        color = InventraInk,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onProductSelect(product) }
                                .padding(vertical = 10.dp),
                    )
                    HorizontalDivider(color = InventraPurple.copy(alpha = 0.1f))
                }
            }
        form.query.trim().length >= MIN_QUERY_FOR_EMPTY ->
            Text(
                text = "Nenhum produto encontrado.",
                fontFamily = Montserrat,
                fontSize = 13.sp,
                color = InventraMuted,
            )
    }
}

private const val MAX_RESULTS = 6
private const val MIN_QUERY_FOR_EMPTY = 2

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ItemFormContentPreview() {
    ItemFormContent(
        form =
            ItemForm(
                query = "lei",
                results = listOf(ProductOption(1, "Leite Integral", "L"), ProductOption(2, "Leite Condensado", "un")),
            ),
        type = RequisitionType.PURCHASE,
        suppliers = listOf(SupplierOption(1, "Italac"), SupplierOption(2, "Seara Alimentos")),
        isSaving = false,
        actions = ItemFormActions(),
    )
}
