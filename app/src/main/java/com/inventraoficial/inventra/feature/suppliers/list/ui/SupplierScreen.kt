package com.inventraoficial.inventra.feature.suppliers.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraSearchWithFilter
import com.inventraoficial.inventra.core.designsystem.molecules.InventraSupplierRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun SupplierScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterIconClick: () -> Unit,
    suppliers: List<Supplier>,
    isLoading: Boolean,
    errorMessage: String?,
    onSupplierClick: (Supplier) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeFilterCount: Int = 0,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            title = {
                Text(
                    "Fornecedores",
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
                    .padding(16.dp),
        ) {
            InventraSearchWithFilter(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onFilterClick = onFilterIconClick,
                placeholder = "Buscar fornecedor...",
                activeFilterCount = activeFilterCount,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            when {
                isLoading -> CenteredLoading(modifier = Modifier.weight(1f))
                errorMessage != null ->
                    ErrorState(message = errorMessage, onRetryClick = onRetryClick, modifier = Modifier.weight(1f))
                suppliers.isEmpty() ->
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nenhum fornecedor encontrado.",
                            fontFamily = Montserrat,
                            fontSize = 14.sp,
                            color = InventraMuted,
                        )
                    }
                else ->
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(suppliers, key = { it.id }) { supplier ->
                            InventraSupplierRow(
                                painter = painterResource(supplier.imageRes),
                                name = supplier.name,
                                rating = supplier.rating,
                                badgeText = supplier.badgeText,
                                onClick = { onSupplierClick(supplier) },
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun CenteredLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = InventraPurple)
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            fontFamily = Montserrat,
            fontSize = 15.sp,
            color = InventraInk,
            textAlign = TextAlign.Center,
        )
        InventraSecondaryButton(text = "Tentar novamente", onClick = onRetryClick)
    }
}

@Composable
fun SupplierRoute(
    navigator: Navigator,
    viewModel: SupplierViewModel = viewModel(factory = SupplierViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    SupplierScreen(
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterIconClick = viewModel::onFilterIconClick,
        suppliers = uiState.suppliers,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onSupplierClick = { supplier -> navigator.navigate(Screen.SupplierDetail(supplier.id)) },
        onRetryClick = viewModel::onRetryClick,
        activeFilterCount = uiState.filters.activeCount,
        modifier = modifier,
    )

    uiState.draftFilters?.let { draft ->
        SupplierFiltersSheet(
            filters = draft,
            actions =
                SupplierFiltersActions(
                    onSortSelect = viewModel::onSortSelect,
                    onMinRatingSelect = viewModel::onMinRatingSelect,
                    onContactToggle = viewModel::onContactToggle,
                    onClear = viewModel::onClearFilters,
                    onApply = viewModel::onApplyFilters,
                ),
            onDismiss = viewModel::onFiltersDismiss,
        )
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview
@Composable
private fun SupplierScreenPreview() {
    SupplierScreen(
        searchQuery = "",
        onSearchQueryChange = {},
        onFilterIconClick = {},
        suppliers =
            listOf(
                Supplier(1, R.drawable.ic_inventra_logo, "Seara Alimentos", 5, "02.914.460/0001-50"),
                Supplier(2, R.drawable.ic_inventra_logo, "Piracanjuba", 4, "01.234.567/0001-89"),
            ),
        isLoading = false,
        errorMessage = null,
        onSupplierClick = {},
        onRetryClick = {},
    )
}
