package com.inventraoficial.inventra.feature.cozinha.select.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraCloseButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRequestStatus
import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRow
import com.inventraoficial.inventra.core.designsystem.molecules.InventraSearchWithFilter
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun SelectCozinhaScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterIconClick: () -> Unit,
    viewMode: CozinhaViewMode,
    onViewModeSelect: (CozinhaViewMode) -> Unit,
    cozinhas: List<CozinhaListItem>,
    onCozinhaClick: (CozinhaListItem) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        InventraTopBar(
            navigationIcon = {
                InventraCloseButton(onClick = onCloseClick)
            },
            title = {
                Text(
                    "Selecionar cozinha",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
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
                placeholder = "Buscar cozinha...",
                modifier = Modifier.padding(bottom = 16.dp),
            )
            ViewModeToggle(
                selected = viewMode,
                onSelect = onViewModeSelect,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            if (viewMode == CozinhaViewMode.List) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(cozinhas) { cozinha ->
                        InventraCozinhaRow(
                            name = cozinha.name,
                            subtitle = cozinha.subtitle,
                            status = cozinha.status,
                            onClick = { onCozinhaClick(cozinha) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            } else {
                MapPlaceholder(modifier = Modifier.weight(1f))
            }
        }
    }
}

private const val TOGGLE_CORNER_PERCENT = 50

@Composable
private fun ViewModeToggle(
    selected: CozinhaViewMode,
    onSelect: (CozinhaViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val toggleShape = RoundedCornerShape(percent = TOGGLE_CORNER_PERCENT)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), toggleShape),
    ) {
        CozinhaViewMode.entries.forEach { mode ->
            val isSelected = mode == selected
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .background(if (isSelected) InventraPurple else Color.White, toggleShape)
                        .clickable(onClick = { onSelect(mode) })
                        .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (mode == CozinhaViewMode.List) "Lista" else "Mapa",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else InventraPurple,
                )
            }
        }
    }
}

@Composable
private fun MapPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(InventraPurple.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = InventraPurple,
                modifier = Modifier.height(40.dp),
            )
            Text(
                text = "Mapa em breve",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                color = InventraPurple,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
fun SelectCozinhaRoute(
    navigator: Navigator,
    viewModel: SelectCozinhaViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    SelectCozinhaScreen(
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterIconClick = viewModel::onFilterIconClick,
        viewMode = uiState.viewMode,
        onViewModeSelect = viewModel::onViewModeSelect,
        cozinhas = uiState.cozinhas,
        onCozinhaClick = { cozinha ->
            if (cozinha.status == InventraCozinhaRequestStatus.Disponivel) {
                navigator.navigate(Screen.RequestSent)
            }
        },
        onCloseClick = { navigator.replaceStack(Screen.Login) },
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun SelectCozinhaScreenPreview() {
    SelectCozinhaScreen(
        searchQuery = "",
        onSearchQueryChange = {},
        onFilterIconClick = {},
        viewMode = CozinhaViewMode.List,
        onViewModeSelect = {},
        cozinhas =
            listOf(
                CozinhaListItem(
                    name = "Restaurante Sabor Caseiro",
                    subtitle = "Matriz · São Paulo, SP",
                    status = InventraCozinhaRequestStatus.Disponivel,
                ),
                CozinhaListItem(
                    name = "Tung Tung Tung Sahur",
                    subtitle = "Filial · Rio de Janeiro, RJ",
                    status = InventraCozinhaRequestStatus.Pendente,
                ),
            ),
        onCozinhaClick = {},
        onCloseClick = {},
    )
}
