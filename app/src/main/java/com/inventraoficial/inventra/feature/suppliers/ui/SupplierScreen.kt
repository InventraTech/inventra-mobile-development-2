package com.inventraoficial.inventra.feature.suppliers.ui

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
import com.inventraoficial.inventra.core.designsystem.molecules.InventraSearchWithFilter
import com.inventraoficial.inventra.core.designsystem.molecules.InventraSupplierRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomBar
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomDestination
import com.inventraoficial.inventra.core.designsystem.organisms.InventraFilterChipRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraQrScanFab
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun SupplierScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterIconClick: () -> Unit,
    filterOptions: List<String>,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    suppliers: List<Supplier>,
    onSupplierClick: (Supplier) -> Unit,
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
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                InventraFilterChipRow(
                    options = filterOptions,
                    selected = selectedFilter,
                    onSelect = onFilterSelect,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(suppliers) { supplier ->
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
fun SupplierRoute(
    viewModel: SupplierViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    SupplierScreen(
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterIconClick = viewModel::onFilterIconClick,
        filterOptions = uiState.filterOptions,
        selectedFilter = uiState.selectedFilter,
        onFilterSelect = viewModel::onFilterSelect,
        suppliers = uiState.suppliers,
        onSupplierClick = viewModel::onSupplierClick,
        onQrScanClick = viewModel::onQrScanClick,
        selectedDestination = uiState.selectedDestination,
        onBottomNavSelect = viewModel::onBottomNavSelect,
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview
@Composable
private fun SupplierScreenPreview() {
    SupplierScreen(
        searchQuery = "",
        onSearchQueryChange = {},
        onFilterIconClick = {},
        filterOptions = listOf("Todos", "Carnes", "Laticínios", "Grãos"),
        selectedFilter = "Todos",
        onFilterSelect = {},
        suppliers =
            listOf(
                Supplier(R.drawable.ic_inventra_logo, "Seara Alimentos", 5, "VPQ 50", "Carnes"),
                Supplier(R.drawable.ic_inventra_logo, "Piracanjuba", 4, "VPQ 41", "Laticínios"),
            ),
        onSupplierClick = {},
        onQrScanClick = {},
        selectedDestination = InventraBottomDestination.Stock,
        onBottomNavSelect = {},
    )
}
