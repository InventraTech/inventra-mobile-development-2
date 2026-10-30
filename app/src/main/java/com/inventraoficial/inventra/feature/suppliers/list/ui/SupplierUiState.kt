package com.inventraoficial.inventra.feature.suppliers.list.ui

data class Supplier(
    val id: Int,
    val photoUrl: String? = null,
    val name: String,
    val rating: Int,
    val badgeText: String,
)

data class SupplierUiState(
    val searchQuery: String = "",
    val suppliers: List<Supplier> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val filters: SupplierFilters = SupplierFilters(),
    val draftFilters: SupplierFilters? = null,
)

data class SupplierFilters(
    val sort: SupplierSort = SupplierSort.Name,
    val minRating: MinRating = MinRating.Any,
    val contacts: Set<SupplierContact> = emptySet(),
) {
    val activeCount: Int
        get() =
            contacts.size +
                (if (sort == SupplierSort.Name) 0 else 1) +
                (if (minRating == MinRating.Any) 0 else 1)
}

enum class SupplierSort(
    val label: String,
) {
    Name("Nome (A–Z)"),
    Rating("Melhor avaliação"),
}

@Suppress("MagicNumber")
enum class MinRating(
    val label: String,
    val stars: Int,
) {
    Any("Qualquer", 0),
    Three("3 ou mais", 3),
    Four("4 ou mais", 4),
    Five("5 estrelas", 5),
}

enum class SupplierContact(
    val label: String,
) {
    WhatsApp("Tem WhatsApp"),
    Email("Tem e-mail"),
}

data class SupplierFiltersActions(
    val onSortSelect: (SupplierSort) -> Unit = {},
    val onMinRatingSelect: (MinRating) -> Unit = {},
    val onContactToggle: (SupplierContact) -> Unit = {},
    val onClear: () -> Unit = {},
    val onApply: () -> Unit = {},
)
