package com.inventraoficial.inventra.feature.stock.list.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraFilterChip
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockFiltersSheet(
    filters: StockFilters,
    categoryOptions: List<String>,
    actions: StockFiltersActions,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = InventraBackground,
    ) {
        StockFiltersContent(filters = filters, categoryOptions = categoryOptions, actions = actions)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StockFiltersContent(
    filters: StockFilters,
    categoryOptions: List<String>,
    actions: StockFiltersActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Filtros",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = InventraInk,
                modifier = Modifier.weight(1f),
            )
            InventraTextLink(text = "Limpar", onClick = actions.onClear, color = InventraDanger)
        }
        FilterSection("Ordenar por") {
            StockSort.entries.forEach { sort ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { actions.onSortSelect(sort) },
                ) {
                    RadioButton(
                        selected = filters.sort == sort,
                        onClick = { actions.onSortSelect(sort) },
                        colors = RadioButtonDefaults.colors(selectedColor = InventraPurple),
                    )
                    Text(text = sort.label, fontFamily = Montserrat, fontSize = 13.sp, color = InventraInk)
                }
            }
        }
        if (categoryOptions.isNotEmpty()) {
            FilterSection("Categoria") {
                FlowRow(horizontalArrangement = chipSpacing, verticalArrangement = chipSpacing) {
                    categoryOptions.forEach { category ->
                        InventraFilterChip(
                            label = category,
                            selected = category in filters.categories,
                            onClick = { actions.onCategoryToggle(category) },
                        )
                    }
                }
            }
        }
        FilterSection("Status") {
            FlowRow(horizontalArrangement = chipSpacing, verticalArrangement = chipSpacing) {
                StockStatus.entries.forEach { status ->
                    InventraFilterChip(
                        label = status.label,
                        selected = status in filters.statuses,
                        onClick = { actions.onStatusToggle(status) },
                    )
                }
            }
        }
        InventraPrimaryButton(text = "Aplicar filtros", onClick = actions.onApply, modifier = Modifier.fillMaxWidth())
    }
}

private val chipSpacing = Arrangement.spacedBy(8.dp)

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = InventraPurple,
        )
        content()
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun StockFiltersContentPreview() {
    StockFiltersContent(
        filters =
            StockFilters(
                sort = StockSort.Expiration,
                categories = setOf("Laticínios"),
                statuses = setOf(StockStatus.ExpiringSoon),
            ),
        categoryOptions = listOf("Bebidas", "Carnes", "Grãos", "Laticínios", "Limpeza"),
        actions = StockFiltersActions(),
    )
}
