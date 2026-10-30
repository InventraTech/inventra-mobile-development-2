package com.inventraoficial.inventra.feature.prelist.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraProductImage
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSectionLabel
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.feature.requirements.RequisitionsError
import com.inventraoficial.inventra.feature.requirements.RequisitionsLoading
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun PreListDetailScreen(
    uiState: PreListDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onConvertClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            navigationIcon = { InventraBackButton(onClick = onBackClick) },
            title = {
                Text(
                    "Pré-lista",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
            },
        )
        when {
            uiState.isLoading -> RequisitionsLoading(modifier = Modifier.weight(1f))
            uiState.errorMessage != null ->
                RequisitionsError(
                    message = uiState.errorMessage,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.weight(1f),
                )
            else -> {
                PreListContent(uiState = uiState, modifier = Modifier.weight(1f))
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.convertErrorMessage?.let {
                        Text(text = it, color = InventraDanger, fontFamily = Montserrat, fontSize = 13.sp)
                    }
                    InventraPrimaryButton(
                        text = "Transformar em compra",
                        onClick = onConvertClick,
                        isLoading = uiState.isConverting,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun PreListContent(
    uiState: PreListDetailUiState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "header") {
            Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Lista de ${uiState.dateText}",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = InventraPurple,
                )
                Text(text = uiState.summaryText, fontFamily = Montserrat, fontSize = 13.sp, color = InventraMuted)
            }
        }
        uiState.groups.forEach { group ->
            item(key = "supplier-${group.supplierName}") {
                InventraSectionLabel(text = group.supplierName, modifier = Modifier.padding(top = 8.dp))
            }
            items(group.items, key = { it.id }) { item -> PreListItemRow(item = item) }
        }
    }
}

@Composable
private fun PreListItemRow(item: PreListItemUi) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), shape)
                .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        InventraProductImage(photoUrl = item.photoUrl, modifier = Modifier.size(44.dp))
        Text(
            text = item.name,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = InventraPurple,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(text = item.quantityText, fontFamily = Montserrat, fontSize = 13.sp, color = InventraMuted)
    }
}

@Composable
fun PreListDetailRoute(
    preListId: Int,
    navigator: Navigator,
    viewModel: PreListDetailViewModel =
        viewModel(key = "pre-list-$preListId", factory = PreListDetailViewModel.factory(preListId)),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.openedDraftId) {
        uiState.openedDraftId?.let { draftId ->
            viewModel.onDraftOpened()
            navigator.navigate(Screen.RequisitionEditor(draftId))
        }
    }

    PreListDetailScreen(
        uiState = uiState,
        onBackClick = { navigator.back() },
        onRetryClick = viewModel::onRetryClick,
        onConvertClick = viewModel::onConvertClick,
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun PreListDetailScreenPreview() {
    PreListDetailScreen(
        uiState =
            PreListDetailUiState(
                isLoading = false,
                dateText = "24/10",
                summaryText = "3 itens · 2 fornecedores",
                groups =
                    listOf(
                        PreListSupplierGroup(
                            supplierName = "Italac Distribuidora",
                            items =
                                listOf(
                                    PreListItemUi(
                                        id = 1,
                                        name = "Leite Integral",
                                        photoUrl = null,
                                        quantityText = "Qtd. 12",
                                    ),
                                    PreListItemUi(
                                        id = 2,
                                        name = "Manteiga",
                                        photoUrl = null,
                                        quantityText = "Qtd. 4",
                                    ),
                                ),
                        ),
                        PreListSupplierGroup(
                            supplierName = "Seara Alimentos",
                            items =
                                listOf(
                                    PreListItemUi(
                                        id = 3,
                                        name = "Frango Congelado",
                                        photoUrl = null,
                                        quantityText = "Qtd. 8,5",
                                    ),
                                ),
                        ),
                    ),
            ),
        onBackClick = {},
        onRetryClick = {},
        onConvertClick = {},
    )
}
