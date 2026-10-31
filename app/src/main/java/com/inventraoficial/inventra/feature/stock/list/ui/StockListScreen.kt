package com.inventraoficial.inventra.feature.stock.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.inventraoficial.inventra.core.designsystem.molecules.InventraProductRow
import com.inventraoficial.inventra.core.designsystem.molecules.InventraSearchWithFilter
import com.inventraoficial.inventra.core.designsystem.organisms.InventraFilterChipRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScreenContent
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun StockListScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterIconClick: () -> Unit,
    filterOptions: List<String>,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    products: List<Product>,
    isLoading: Boolean,
    errorMessage: String?,
    onProductClick: (Product) -> Unit,
    onScanClick: () -> Unit,
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
                    "Estoque",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontSize = 17.sp,
                )
            },
            actions = {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = "Escanear produto",
                    tint = Color.White,
                    modifier = Modifier.clickable(onClick = onScanClick),
                )
            },
        )
        InventraScreenContent {
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
                    placeholder = "Buscar produto...",
                    activeFilterCount = activeFilterCount,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                InventraFilterChipRow(
                    options = filterOptions,
                    selected = selectedFilter,
                    onSelect = onFilterSelect,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                when {
                    isLoading -> CenteredLoading(modifier = Modifier.weight(1f))
                    errorMessage != null ->
                        ErrorState(message = errorMessage, onRetryClick = onRetryClick, modifier = Modifier.weight(1f))
                    products.isEmpty() ->
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Nenhum produto encontrado.",
                                fontFamily = Montserrat,
                                fontSize = 14.sp,
                                color = InventraMuted,
                            )
                        }
                    else ->
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(products, key = { it.id }) { product ->
                                InventraProductRow(
                                    painter = painterResource(product.imageRes),
                                    title = product.title,
                                    quantity = product.quantity,
                                    batchInfo = product.batchInfo,
                                    onClick = { onProductClick(product) },
                                )
                            }
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
fun StockListRoute(
    navigator: Navigator,
    viewModel: StockListViewModel = viewModel(factory = StockListViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onScreenVisible()
    }

    StockListScreen(
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterIconClick = viewModel::onFilterIconClick,
        filterOptions = uiState.filterOptions,
        selectedFilter = uiState.selectedFilter,
        onFilterSelect = viewModel::onFilterSelect,
        products = uiState.products,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onProductClick = { product -> navigator.navigate(Screen.StockDetail(product.id)) },
        onScanClick = { navigator.navigate(Screen.Scan) },
        onRetryClick = viewModel::onRetryClick,
        activeFilterCount = uiState.filters.activeCount,
        modifier = modifier,
    )

    uiState.draftFilters?.let { draft ->
        StockFiltersSheet(
            filters = draft,
            categoryOptions = uiState.categoryOptions,
            actions =
                StockFiltersActions(
                    onSortSelect = viewModel::onSortSelect,
                    onCategoryToggle = viewModel::onCategoryToggle,
                    onStatusToggle = viewModel::onStatusToggle,
                    onClear = viewModel::onClearFilters,
                    onApply = viewModel::onApplyFilters,
                ),
            onDismiss = viewModel::onFiltersDismiss,
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun StockListScreenPreview() {
    StockListScreen(
        searchQuery = "",
        onSearchQueryChange = {},
        onFilterIconClick = {},
        filterOptions = listOf("Todos", "Carnes", "Laticínios", "Grãos"),
        selectedFilter = "Todos",
        onFilterSelect = {},
        products =
            listOf(
                Product(1, R.drawable.ic_inventra_logo, "Leite Integral", "18 L", "3 lotes", "Laticínios"),
                Product(2, R.drawable.ic_inventra_logo, "Arroz Camil", "2 kg", "1 lote · abaixo do mínimo", "Grãos"),
            ),
        isLoading = false,
        errorMessage = null,
        onProductClick = {},
        onScanClick = {},
        onRetryClick = {},
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun StockListScreenErrorPreview() {
    StockListScreen(
        searchQuery = "",
        onSearchQueryChange = {},
        onFilterIconClick = {},
        filterOptions = listOf("Todos"),
        selectedFilter = "Todos",
        onFilterSelect = {},
        products = emptyList(),
        isLoading = false,
        errorMessage = "Não foi possível conectar ao servidor. Verifique sua conexão.",
        onProductClick = {},
        onScanClick = {},
        onRetryClick = {},
    )
}
