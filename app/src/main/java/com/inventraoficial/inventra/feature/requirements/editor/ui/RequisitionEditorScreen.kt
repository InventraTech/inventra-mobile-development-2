package com.inventraoficial.inventra.feature.requirements.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraFilterChip
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.core.designsystem.molecules.InventraConfirmDialog
import com.inventraoficial.inventra.core.designsystem.organisms.InventraChipGroup
import com.inventraoficial.inventra.core.designsystem.organisms.InventraFilterSection
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScreenContent
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType
import com.inventraoficial.inventra.feature.requirements.label
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun RequisitionEditorScreen(
    uiState: RequisitionEditorUiState,
    actions: RequisitionEditorActions,
    modifier: Modifier = Modifier,
) {
    var confirmDiscard by remember { mutableStateOf(false) }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            navigationIcon = { InventraBackButton(onClick = actions.onBackClick) },
            title = {
                Text(
                    text = uiState.requisitionId?.let { "Rascunho #$it" } ?: "Nova requisição",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontSize = 17.sp,
                )
            },
        )
        InventraScreenContent {
            if (uiState.isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = InventraPurple)
                }
            } else {
                EditorContent(
                    uiState = uiState,
                    actions = actions,
                    onDiscardClick = { confirmDiscard = true },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    if (confirmDiscard) {
        InventraConfirmDialog(
            title = "Descartar rascunho?",
            message = "Os itens adicionados serão perdidos.",
            confirmText = "Descartar",
            onConfirm = {
                confirmDiscard = false
                actions.onDiscardClick()
            },
            onDismiss = { confirmDiscard = false },
            dismissText = "Voltar",
            isDestructive = true,
        )
    }
}

@Composable
private fun EditorContent(
    uiState: RequisitionEditorUiState,
    actions: RequisitionEditorActions,
    onDiscardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        InventraFilterSection("Tipo") {
            InventraChipGroup {
                TYPE_ORDER.forEach { type ->
                    InventraFilterChip(
                        label = type.label,
                        selected = uiState.type == type,
                        onClick = { actions.onTypeSelect(type) },
                    )
                }
            }
        }
        InventraLabeledField(
            label = originLabel(uiState.type),
            value = uiState.origin,
            onValueChange = actions.onOriginChange,
            readOnly = uiState.isDraftCreated,
            placeholder = originPlaceholder(uiState.type),
            modifier = Modifier.fillMaxWidth(),
        )
        ItemsSection(uiState = uiState, actions = actions)
        uiState.errorMessage?.let {
            Text(text = it, fontFamily = Montserrat, fontSize = 14.sp, color = InventraDanger)
        }
        InventraPrimaryButton(
            text = "Enviar para análise",
            onClick = actions.onSubmitClick,
            enabled = uiState.canSubmit,
            isLoading = uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
        )
        InventraTextLink(
            text = "Descartar rascunho",
            onClick = onDiscardClick,
            color = InventraDanger,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun ItemsSection(
    uiState: RequisitionEditorUiState,
    actions: RequisitionEditorActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Itens (${uiState.items.size})",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = InventraInk,
                modifier = Modifier.weight(1f),
            )
            uiState.totalText?.let {
                Text(text = "Total est. $it", fontFamily = Montserrat, fontSize = 14.sp, color = InventraMuted)
            }
        }
        if (uiState.items.isEmpty()) {
            Text(text = "Nenhum item ainda.", fontFamily = Montserrat, fontSize = 14.sp, color = InventraMuted)
        }
        uiState.items.forEach { item ->
            EditorItemCard(item = item, onRemoveClick = { actions.onRemoveItemClick(item.id) })
        }
        InventraSecondaryButton(
            text = "Adicionar item",
            onClick = actions.onAddItemClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EditorItemCard(
    item: EditorItemUi,
    onRemoveClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), shape)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.name,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = InventraPurple,
            )
            Text(text = item.quantityText, fontFamily = Montserrat, fontSize = 14.sp, color = InventraInk)
            item.details?.let { Text(text = it, fontFamily = Montserrat, fontSize = 13.sp, color = InventraMuted) }
        }
        InventraTextLink(text = "Remover", onClick = onRemoveClick, color = InventraDanger)
    }
}

private val TYPE_ORDER = listOf(RequisitionType.PURCHASE, RequisitionType.CONSUMPTION, RequisitionType.TRANSFER)

private fun originPlaceholder(type: RequisitionType): String =
    when (type) {
        RequisitionType.PURCHASE -> "Ex.: Reposição semanal"
        RequisitionType.CONSUMPTION -> "Ex.: Produção do almoço"
        RequisitionType.TRANSFER -> "Ex.: Filial Centro"
    }

@Composable
fun RequisitionEditorRoute(
    requisitionId: Int?,
    navigator: Navigator,
    viewModel: RequisitionEditorViewModel =
        viewModel(
            key = "requisition-editor-${requisitionId ?: "new"}",
            factory = RequisitionEditorViewModel.factory(requisitionId),
        ),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onScreenVisible()
    }

    LaunchedEffect(uiState.result) {
        if (uiState.result != null) navigator.back()
    }

    RequisitionEditorScreen(
        uiState = uiState,
        actions =
            RequisitionEditorActions(
                onTypeSelect = viewModel::onTypeSelect,
                onOriginChange = viewModel::onOriginChange,
                onAddItemClick = viewModel::onAddItemClick,
                onRemoveItemClick = viewModel::onRemoveItemClick,
                onSubmitClick = viewModel::onSubmitClick,
                onDiscardClick = viewModel::onDiscardClick,
                onRetryClick = viewModel::onRetryClick,
                onBackClick = { navigator.back() },
            ),
        modifier = modifier,
    )

    uiState.itemForm?.let { form ->
        ItemFormSheet(
            form = form,
            type = uiState.type,
            suppliers = uiState.suppliers,
            isSaving = uiState.isSaving,
            actions =
                ItemFormActions(
                    onQueryChange = viewModel::onQueryChange,
                    onProductSelect = viewModel::onProductSelect,
                    onQuantityChange = viewModel::onQuantityChange,
                    onPriceChange = viewModel::onPriceChange,
                    onSupplierSelect = viewModel::onSupplierSelect,
                    onNoteChange = viewModel::onNoteChange,
                    onConfirm = viewModel::onConfirmItem,
                    onDismiss = viewModel::onItemFormDismiss,
                ),
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(heightDp = 900)
@Composable
private fun RequisitionEditorScreenPreview() {
    RequisitionEditorScreen(
        uiState =
            RequisitionEditorUiState(
                requisitionId = 14,
                origin = "Reposição semanal",
                items =
                    listOf(
                        EditorItemUi(1, "Leite Integral", "Qtd. 20", "Preço est. R$ 4,50 · Italac"),
                        EditorItemUi(2, "Arroz Camil", "Qtd. 10", null),
                    ),
                totalText = "R$ 90,00",
            ),
        actions = RequisitionEditorActions(),
    )
}
