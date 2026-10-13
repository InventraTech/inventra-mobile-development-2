package com.inventraoficial.inventra.feature.stock.detail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBatch
import com.inventraoficial.inventra.data.remote.dto.product.ProductResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockBatchStatus
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementResponse
import com.inventraoficial.inventra.data.remote.dto.stock.StockMovementType
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.ProductRepository
import com.inventraoficial.inventra.data.repository.StockRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import java.util.Locale

class StockDetailViewModel(
    private val productId: Int,
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
    private val authRepository: AuthRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val today: () -> LocalDate = { LocalDate.now(zone) },
) : ViewModel() {
    private val _uiState = MutableStateFlow(StockDetailUiState())
    val uiState: StateFlow<StockDetailUiState> = _uiState.asStateFlow()

    private var unitSymbol = ""
    private var usableBatches: List<StockBatchResponse> = emptyList()
    private var isBatchesExpanded = false

    fun onScreenVisible() {
        load()
    }

    fun onRetryClick() {
        load()
    }

    fun onSeeAllBatchesClick() {
        isBatchesExpanded = !isBatchesExpanded
        refreshBatches()
    }

    fun onEntriesClick() {
        toggleHistory(HistoryType.Entries)
    }

    fun onExitsClick() {
        toggleHistory(HistoryType.Exits)
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = it.productName.isEmpty(), errorMessage = null) }
        viewModelScope.launch {
            val kitchenId = authRepository.currentUser.first()?.kitchenId
            val product = async { productRepository.product(productId) }
            val batches =
                async {
                    kitchenId?.let { stockRepository.batches(it, productId) } ?: Result.success(emptyList())
                }
            val productResult = product.await()
            val batchesResult = batches.await()
            val error = productResult.exceptionOrNull() ?: batchesResult.exceptionOrNull()
            if (error != null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                return@launch
            }
            show(productResult.getOrThrow(), batchesResult.getOrThrow())
        }
    }

    private fun show(
        product: ProductResponse,
        batches: List<StockBatchResponse>,
    ) {
        val today = today()
        unitSymbol = product.unit.symbol
        usableBatches =
            batches
                .filter { it.status == StockBatchStatus.ACTIVE && !LocalDate.parse(it.expirationDate).isBefore(today) }
                .sortedBy { it.expirationDate }
        val total = usableBatches.sumOf { it.currentQuantity }
        val latestSupplier = batches.maxByOrNull { it.entryDate }?.supplier?.legalName
        _uiState.update {
            it.copy(
                productName = product.name,
                productDetails = product.details(),
                totalValue = "${formatQuantity(total)} $unitSymbol",
                totalCaption =
                    when (usableBatches.size) {
                        0 -> "Nenhum lote disponível na cozinha."
                        1 -> "Em 1 lote dentro da validade."
                        else -> "Em ${usableBatches.size} lotes dentro da validade."
                    },
                supplier = latestSupplier ?: "Sem fornecedor informado",
                isLoading = false,
            )
        }
        refreshBatches()
        _uiState.value.history?.let { loadHistory(it.type) }
    }

    private fun refreshBatches() {
        val today = today()
        val visible = if (isBatchesExpanded) usableBatches else usableBatches.take(COLLAPSED_BATCHES)
        _uiState.update {
            it.copy(
                batches = visible.map { batch -> batch.toInventraBatch(today, unitSymbol) },
                seeAllBatchesText =
                    when {
                        usableBatches.size <= COLLAPSED_BATCHES -> null
                        isBatchesExpanded -> "Mostrar menos"
                        else -> "Ver todos os ${usableBatches.size} lotes"
                    },
            )
        }
    }

    private fun toggleHistory(type: HistoryType) {
        if (_uiState.value.history?.type == type) {
            _uiState.update { it.copy(history = null) }
            return
        }
        loadHistory(type)
    }

    private fun loadHistory(type: HistoryType) {
        _uiState.update { it.copy(history = HistoryState(type = type, isLoading = true)) }
        viewModelScope.launch {
            val kitchenId = authRepository.currentUser.first()?.kitchenId
            val result =
                if (kitchenId == null) {
                    Result.success(emptyList())
                } else {
                    stockRepository.movements(kitchenId, productId, type.movementType())
                }
            if (_uiState.value.history?.type != type) return@launch
            val history =
                result.fold(
                    onSuccess = { movements ->
                        HistoryState(type, movements.map { it.toHistoryEntry(type, unitSymbol, zone) })
                    },
                    onFailure = { error -> HistoryState(type, errorMessage = error.message) },
                )
            _uiState.update { it.copy(history = history) }
        }
    }

    companion object {
        fun factory(productId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    StockDetailViewModel(
                        productId = productId,
                        productRepository = application.container.productRepository,
                        stockRepository = application.container.stockRepository,
                        authRepository = application.container.authRepository,
                    )
                }
            }
    }
}

private const val COLLAPSED_BATCHES = 3
private const val URGENT_DAYS = 2

private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
private val quantityFormat = DecimalFormat("#,##0.###", DecimalFormatSymbols(ptBr))
private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

private fun formatQuantity(value: Double): String = quantityFormat.format(value)

private fun ProductResponse.details(): String? {
    val parts = listOfNotNull(brand, category?.name)
    return if (parts.isEmpty()) null else parts.joinToString(" · ")
}

private fun StockBatchResponse.toInventraBatch(
    today: LocalDate,
    unitSymbol: String,
): InventraBatch {
    val expiration = LocalDate.parse(expirationDate)
    val days = DAYS.between(today, expiration)
    return InventraBatch(
        dateLabel =
            when (days) {
                0L -> "HOJE"
                1L -> "AMANHÃ"
                else -> expiration.format(dateFormatter)
            },
        code = batchNumber,
        quantity = "${formatQuantity(currentQuantity)} $unitSymbol",
        isUrgent = days <= URGENT_DAYS,
    )
}

private fun StockMovementResponse.toHistoryEntry(
    type: HistoryType,
    unitSymbol: String,
    zone: ZoneId,
): HistoryEntry {
    val created = LocalDateTime.parse(createdAt).atZone(ZoneOffset.UTC).withZoneSameInstant(zone)
    val sign = if (type == HistoryType.Entries) "+" else "−"
    val who = user?.name ?: "Sistema"
    return HistoryEntry(
        date = created.format(dateTimeFormatter),
        title = reason?.let { "$who · ${reasonLabel(it)}" } ?: who,
        quantityLabel = "$sign${formatQuantity(quantity)} $unitSymbol · lote ${batch.batchNumber}",
    )
}

private fun HistoryType.movementType(): StockMovementType =
    when (this) {
        HistoryType.Entries -> StockMovementType.ENTRY
        HistoryType.Exits -> StockMovementType.WRITE_OFF
    }

private fun reasonLabel(reason: String): String =
    when (reason) {
        "USAGE" -> "Uso"
        "LOSS" -> "Perda"
        "EXPIRED" -> "Vencido"
        "REQUISITION" -> "Requisição"
        else -> reason
    }
