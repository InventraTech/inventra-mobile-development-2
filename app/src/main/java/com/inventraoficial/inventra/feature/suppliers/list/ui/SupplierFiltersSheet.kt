package com.inventraoficial.inventra.feature.suppliers.list.ui

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
fun SupplierFiltersSheet(
    filters: SupplierFilters,
    actions: SupplierFiltersActions,
    onDismiss: () -> Unit,
) {
    InventraFiltersSheet(onDismiss = onDismiss, onClear = actions.onClear, onApply = actions.onApply) {
        SupplierFilterSections(filters, actions)
    }
}

@Composable
private fun ColumnScope.SupplierFilterSections(
    filters: SupplierFilters,
    actions: SupplierFiltersActions,
) {
    InventraFilterSection("Ordenar por") {
        SupplierSort.entries.forEach { sort ->
            InventraRadioOption(
                label = sort.label,
                selected = filters.sort == sort,
                onClick = { actions.onSortSelect(sort) },
            )
        }
    }
    InventraFilterSection("Avaliação") {
        InventraChipGroup {
            MinRating.entries.forEach { rating ->
                InventraFilterChip(
                    label = rating.label,
                    selected = filters.minRating == rating,
                    onClick = { actions.onMinRatingSelect(rating) },
                )
            }
        }
    }
    InventraFilterSection("Contato") {
        InventraChipGroup {
            SupplierContact.entries.forEach { contact ->
                InventraFilterChip(
                    label = contact.label,
                    selected = contact in filters.contacts,
                    onClick = { actions.onContactToggle(contact) },
                )
            }
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun SupplierFiltersPreview() {
    InventraFiltersContent(onClear = {}, onApply = {}) {
        SupplierFilterSections(
            filters =
                SupplierFilters(
                    sort = SupplierSort.Rating,
                    minRating = MinRating.Four,
                    contacts = setOf(SupplierContact.WhatsApp),
                ),
            actions = SupplierFiltersActions(),
        )
    }
}
