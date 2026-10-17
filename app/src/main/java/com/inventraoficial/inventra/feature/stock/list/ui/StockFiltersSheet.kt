package com.inventraoficial.inventra.feature.stock.list.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.inventraoficial.inventra.core.designsystem.atoms.InventraFilterChip
import com.inventraoficial.inventra.core.designsystem.organisms.InventraChipGroup
import com.inventraoficial.inventra.core.designsystem.organisms.InventraFilterSection
import com.inventraoficial.inventra.core.designsystem.organisms.InventraFiltersContent
import com.inventraoficial.inventra.core.designsystem.organisms.InventraFiltersSheet
import com.inventraoficial.inventra.core.designsystem.organisms.InventraRadioOption

@Composable
fun StockFiltersSheet(
    filters: StockFilters,
    categoryOptions: List<String>,
    actions: StockFiltersActions,
    onDismiss: () -> Unit,
) {
    InventraFiltersSheet(onDismiss = onDismiss, onClear = actions.onClear, onApply = actions.onApply) {
        StockFilterSections(filters, categoryOptions, actions)
    }
}

@Composable
private fun ColumnScope.StockFilterSections(
    filters: StockFilters,
    categoryOptions: List<String>,
    actions: StockFiltersActions,
) {
    InventraFilterSection("Ordenar por") {
        StockSort.entries.forEach { sort ->
            InventraRadioOption(
                label = sort.label,
                selected = filters.sort == sort,
                onClick = { actions.onSortSelect(sort) },
            )
        }
    }
    if (categoryOptions.isNotEmpty()) {
        InventraFilterSection("Categoria") {
            InventraChipGroup {
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
    InventraFilterSection("Status") {
        InventraChipGroup {
            StockStatus.entries.forEach { status ->
                InventraFilterChip(
                    label = status.label,
                    selected = status in filters.statuses,
                    onClick = { actions.onStatusToggle(status) },
                )
            }
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun StockFiltersPreview() {
    InventraFiltersContent(onClear = {}, onApply = {}) {
        StockFilterSections(
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
}
