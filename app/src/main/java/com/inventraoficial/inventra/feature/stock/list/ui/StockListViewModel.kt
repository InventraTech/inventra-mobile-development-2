package com.inventraoficial.inventra.feature.stock.list.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.local.StockCache
import com.inventraoficial.inventra.data.remote.dto.stock.KitchenStockItemResponse
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import java.util.Locale

@Suppress("TooManyFunctions")
class StockListViewModel(
    private val authRepository: AuthRepository,
    private val stockRepository: StockRepository,
    private val stockCache: StockCache,
    private val today: () -> LocalDate = { LocalDate.now() },
    private val clock: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {
    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState.asStateFlow()

    private var items: List<KitchenStockItemResponse> = emptyList()

    fun onScreenVisible() {
        load()
    }

    fun onRetryClick() {
        load()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshVisibleProducts()
    }

    fun onFilterSelect(filter: String) {
        val categories = if (filter == ALL_FILTER) emptySet() else setOf(filter)
        _uiState.update { it.copy(filters = it.filters.copy(categories = categories)) }
        refreshVisibleProducts()
    }

    fun onFilterIconClick() {
        _uiState.update { it.copy(draftFilters = it.filters) }
    }

    fun onFiltersDismiss() {
        _uiState.update { it.copy(draftFilters = null) }
    }

    fun onSortSelect(sort: StockSort) {
        updateDraft { it.copy(sort = sort) }
    }

    fun onCategoryToggle(category: String) {
        updateDraft { it.copy(categories = it.categories.toggle(category)) }
    }

    fun onStatusToggle(status: StockStatus) {
        updateDraft { it.copy(statuses = it.statuses.toggle(status)) }
    }

    fun onClearFilters() {
        updateDraft { StockFilters() }
    }

    fun onApplyFilters() {
        val draft = _uiState.value.draftFilters ?: return
        _uiState.update { it.copy(filters = draft, draftFilters = null) }
        refreshVisibleProducts()
    }

    private fun updateDraft(transform: (StockFilters) -> StockFilters) {
        _uiState.update { state -> state.copy(draftFilters = state.draftFilters?.let(transform)) }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = items.isEmpty(), errorMessage = null) }
        viewModelScope.launch {
            val kitchenId = authRepository.currentUser.first()?.kitchenId
            if (kitchenId == null) {
                showItems(emptyList())
                return@launch
            }
            val result = stockRepository.kitchenStock(kitchenId)
            val fresh = result.getOrNull()
            val cached = if (fresh == null) stockCache.load(kitchenId) else null
            when {
                fresh != null -> {
                    runCatching { stockCache.save(kitchenId, fresh, clock()) }
                    _uiState.update { it.copy(offlineMessage = null) }
                    showItems(fresh)
                }
                cached != null -> {
                    _uiState.update { it.copy(offlineMessage = cached.savedAt.toOfflineMessage()) }
                    showItems(cached.items)
                }
                else ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message)
                    }
            }
        }
    }

    private fun showItems(newItems: List<KitchenStockItemResponse>) {
        items = newItems
        val categories = newItems.map { it.categoryName() }.distinct().sorted()
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                categoryOptions = categories,
                filters = state.filters.copy(categories = state.filters.categories.intersect(categories.toSet())),
            )
        }
        refreshVisibleProducts()
    }

    private fun refreshVisibleProducts() {
        val state = _uiState.value
        val query = state.searchQuery.trim()
        val filters = state.filters
        val today = today()
        val visible =
            items
                .filter { item ->
                    item.product.name.contains(query, ignoreCase = true) &&
                        (filters.categories.isEmpty() || item.categoryName() in filters.categories) &&
                        (filters.statuses.isEmpty() || filters.statuses.any { item.matches(it, today) })
                }.sortedWith(filters.sort.comparator())
        _uiState.update {
            it.copy(
                products = visible.map { item -> item.toProduct() },
                filterOptions = listOf(ALL_FILTER) + it.categoryOptions,
                selectedFilter =
                    when (filters.categories.size) {
                        0 -> ALL_FILTER
                        1 -> filters.categories.first()
                        else -> ""
                    },
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    StockListViewModel(
                        authRepository = application.container.authRepository,
                        stockRepository = application.container.stockRepository,
                        stockCache = application.container.stockCache,
                    )
                }
            }
    }
}

private const val NO_CATEGORY = "Sem categoria"
private val savedAtFormatter = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm")

private fun Long.toOfflineMessage(): String {
    val savedAt = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault())
    return "Sem conexão. Mostrando o estoque salvo em ${savedAt.format(savedAtFormatter)}."
}

private const val EXPIRING_SOON_DAYS = 7

private val quantityFormat = DecimalFormat("#,##0.###", DecimalFormatSymbols(Locale.forLanguageTag("pt-BR")))

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value

private fun KitchenStockItemResponse.categoryName(): String = category?.name ?: NO_CATEGORY

private fun KitchenStockItemResponse.matches(
    status: StockStatus,
    today: LocalDate,
): Boolean =
    when (status) {
        StockStatus.ExpiringSoon ->
            nextExpirationDate?.let { DAYS.between(today, LocalDate.parse(it)) <= EXPIRING_SOON_DAYS } ?: false
        StockStatus.LowStock -> lowStock
        StockStatus.OutOfStock -> totalQuantity <= 0.0
    }

private fun StockSort.comparator(): Comparator<KitchenStockItemResponse> {
    val byName = compareBy<KitchenStockItemResponse> { it.product.name.lowercase() }
    val byExpiration = compareBy<KitchenStockItemResponse, String?>(nullsLast()) { it.nextExpirationDate }
    return when (this) {
        StockSort.Name -> byName
        StockSort.Expiration -> byExpiration.then(byName)
        StockSort.Quantity -> compareBy<KitchenStockItemResponse> { it.totalQuantity }.then(byName)
    }
}

private fun KitchenStockItemResponse.toProduct(): Product {
    val batches =
        when (activeBatches) {
            0 -> "Sem lotes"
            1 -> "1 lote"
            else -> "$activeBatches lotes"
        }
    return Product(
        id = product.id,
        photoUrl = product.photoUrl,
        title = product.name,
        quantity = "${quantityFormat.format(totalQuantity)} ${unit.symbol}",
        batchInfo = if (lowStock) "$batches · abaixo do mínimo" else batches,
        category = categoryName(),
    )
}
