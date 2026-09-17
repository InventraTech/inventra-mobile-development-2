package com.inventraoficial.inventra.feature.stock.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.molecules.InventraProductRow
import com.inventraoficial.inventra.core.designsystem.molecules.InventraSearchWithFilter
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomBar
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomDestination
import com.inventraoficial.inventra.core.designsystem.organisms.InventraFilterChipRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraQrScanFab
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
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
    onProductClick: (Product) -> Unit,
    onQrScanClick: () -> Unit,
    selectedDestination: InventraBottomDestination,
    onBottomNavSelect: (InventraBottomDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.White),
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
                    placeholder = "Buscar produto...",
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                InventraFilterChipRow(
                    options = filterOptions,
                    selected = selectedFilter,
                    onSelect = onFilterSelect,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(products) { product ->
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
            InventraBottomBar(
                selected = selectedDestination,
                onSelect = onBottomNavSelect,
            )
        }
        InventraQrScanFab(
            onClick = onQrScanClick,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp)
                    .offset(y = (-90).dp),
        )
    }
}

@Composable
fun StockListRoute(
    viewModel: StockListViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    StockListScreen(
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterIconClick = viewModel::onFilterIconClick,
        filterOptions = uiState.filterOptions,
        selectedFilter = uiState.selectedFilter,
        onFilterSelect = viewModel::onFilterSelect,
        products = uiState.products,
        onProductClick = viewModel::onProductClick,
        onQrScanClick = viewModel::onQrScanClick,
        selectedDestination = uiState.selectedDestination,
        onBottomNavSelect = viewModel::onBottomNavSelect,
        modifier = modifier,
    )
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
                Product(R.drawable.ic_inventra_logo, "Leite Integral", "18 L", "3 lotes", "Laticínios"),
                Product(R.drawable.ic_inventra_logo, "Arroz Camil", "24 kg", "2 lotes", "Grãos"),
            ),
        onProductClick = {},
        onQrScanClick = {},
        selectedDestination = InventraBottomDestination.Stock,
        onBottomNavSelect = {},
    )
}
