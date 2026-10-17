package com.inventraoficial.inventra.feature.requirements.editor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.requisition.AddRequisitionItemRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.CreateRequisitionRequest
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionItemResponse
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionStatus
import com.inventraoficial.inventra.data.remote.dto.requisition.RequisitionType
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.ProductRepository
import com.inventraoficial.inventra.data.repository.RequisitionRepository
import com.inventraoficial.inventra.data.repository.SupplierRepository
import com.inventraoficial.inventra.feature.requirements.currencyText
import com.inventraoficial.inventra.feature.requirements.quantityText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
class RequisitionEditorViewModel(
    private val draftId: Int?,
    private val requisitionRepository: RequisitionRepository,
    private val productRepository: ProductRepository,
    private val supplierRepository: SupplierRepository,
    private val authRepository: AuthRepository,
    private val searchDebounceMs: Long = SEARCH_DEBOUNCE_MS,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RequisitionEditorUiState(requisitionId = draftId))
    val uiState: StateFlow<RequisitionEditorUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loaded = false

    fun onScreenVisible() {
        if (loaded) return
        loaded = true
        load()
    }

    fun onRetryClick() {
        load()
    }

    fun onTypeSelect(type: RequisitionType) {
        if (_uiState.value.isDraftCreated) return
        _uiState.update { it.copy(type = type) }
    }

    fun onOriginChange(origin: String) {
        if (_uiState.value.isDraftCreated) return
        _uiState.update { it.copy(origin = origin.take(MAX_ORIGIN_LENGTH), errorMessage = null) }
    }

    fun onAddItemClick() {
        if (_uiState.value.origin.isBlank()) {
            _uiState.update { it.copy(errorMessage = missingOriginMessage(it.type)) }
            return
        }
        _uiState.update { it.copy(itemForm = ItemForm(), errorMessage = null) }
    }

    fun onItemFormDismiss() {
        searchJob?.cancel()
        _uiState.update { it.copy(itemForm = null) }
    }

    fun onQueryChange(query: String) {
        updateForm { it.copy(query = query, product = null, errorMessage = null) }
        searchJob?.cancel()
        val term = query.trim()
        if (term.length < MIN_QUERY_LENGTH) {
            updateForm { it.copy(results = emptyList(), isSearching = false) }
            return
        }
        searchJob =
            viewModelScope.launch {
                delay(searchDebounceMs)
                updateForm { it.copy(isSearching = true) }
                val result = productRepository.search(term)
                updateForm { form ->
                    result.fold(
                        onSuccess = { products ->
                            form.copy(
                                isSearching = false,
                                results = products.map { ProductOption(it.id, it.name, it.unit.symbol) },
                            )
                        },
                        onFailure = { error -> form.copy(isSearching = false, errorMessage = error.message) },
                    )
                }
            }
    }

    fun onProductSelect(product: ProductOption) {
        searchJob?.cancel()
        updateForm { it.copy(product = product, query = product.name, results = emptyList(), errorMessage = null) }
    }

    fun onQuantityChange(quantity: String) {
        updateForm { it.copy(quantity = quantity, errorMessage = null) }
    }

    fun onPriceChange(price: String) {
        updateForm { it.copy(estimatedPrice = price, errorMessage = null) }
    }

    fun onSupplierSelect(supplierId: Int?) {
        updateForm { it.copy(supplierId = supplierId) }
    }

    fun onNoteChange(note: String) {
        updateForm { it.copy(note = note.take(MAX_NOTE_LENGTH)) }
    }

    fun onConfirmItem() {
        val state = _uiState.value
        val form = state.itemForm
        if (form == null || state.isSaving) return
        val product = form.product
        val quantity = form.quantity.toNumberOrNull()
        val price = form.estimatedPrice.toNumberOrNull()
        val error =
            when {
                product == null -> "Escolha um produto da lista."
                quantity == null || quantity <= 0 -> "Informe uma quantidade maior que zero."
                form.estimatedPrice.isNotBlank() && (price == null || price < 0) -> "Informe um preço válido."
                else -> null
            }
        if (error != null || product == null || quantity == null) {
            updateForm { it.copy(errorMessage = error) }
            return
        }
        val request =
            AddRequisitionItemRequest(
                productId = product.id,
                quantity = quantity,
                suggestedSupplierId = form.supplierId.takeIf { state.type == RequisitionType.PURCHASE },
                estimatedPrice = price,
                note = form.note.trim().ifEmpty { null },
            )
        save(onFailure = { message -> updateForm { it.copy(errorMessage = message) } }) {
            val id = ensureDraft() ?: return@save
            requisitionRepository.addItem(id, request).getOrThrow()
            reloadItems(id)
            _uiState.update { it.copy(itemForm = null) }
        }
    }

    fun onRemoveItemClick(itemId: Int) {
        val id = _uiState.value.requisitionId ?: return
        save(onFailure = ::showError) {
            requisitionRepository.removeItem(id, itemId).getOrThrow()
            reloadItems(id)
        }
    }

    fun onSubmitClick() {
        val id = _uiState.value.requisitionId
        if (id == null || !_uiState.value.canSubmit) return
        save(onFailure = ::showError) {
            requisitionRepository.submit(id).getOrThrow()
            _uiState.update { it.copy(result = EditorResult.Submitted) }
        }
    }

    fun onDiscardClick() {
        val id = _uiState.value.requisitionId
        if (id == null) {
            _uiState.update { it.copy(result = EditorResult.Discarded) }
            return
        }
        save(onFailure = ::showError) {
            requisitionRepository.cancel(id, DISCARD_REASON).getOrThrow()
            _uiState.update { it.copy(result = EditorResult.Discarded) }
        }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = draftId != null, errorMessage = null) }
        viewModelScope.launch {
            val kitchenId = authRepository.currentUser.first()?.kitchenId
            if (kitchenId != null) {
                supplierRepository.suppliers(kitchenId).onSuccess { suppliers ->
                    val options = suppliers.map { SupplierOption(it.id, it.legalName) }
                    _uiState.update { state -> state.copy(suppliers = options) }
                }
            }
            if (draftId == null) return@launch
            val requisition =
                requisitionRepository.requisition(draftId).getOrElse { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                    return@launch
                }
            if (requisition.status != RequisitionStatus.DRAFT) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Essa requisição já foi enviada.") }
                return@launch
            }
            _uiState.update { it.copy(type = requisition.type, origin = requisition.origin.orEmpty()) }
            runCatching { reloadItems(draftId) }
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message) } }
        }
    }

    private suspend fun ensureDraft(): Int? {
        val state = _uiState.value
        val kitchenId = authRepository.currentUser.first()?.kitchenId
        return when {
            state.requisitionId != null -> state.requisitionId
            kitchenId == null -> {
                updateForm { it.copy(errorMessage = "Entre em uma cozinha para criar requisições.") }
                null
            }
            else -> {
                val request = CreateRequisitionRequest(state.type, state.origin.trim(), kitchenId)
                val created = requisitionRepository.create(request).getOrThrow()
                _uiState.update { it.copy(requisitionId = created.id) }
                created.id
            }
        }
    }

    private suspend fun reloadItems(id: Int) {
        val items = requisitionRepository.items(id).getOrThrow()
        _uiState.update {
            it.copy(
                items = items.map { item -> item.toUi() },
                totalText =
                    items
                        .filter { item -> item.estimatedPrice != null }
                        .takeIf { priced -> priced.isNotEmpty() }
                        ?.sumOf { item -> item.quantity * (item.estimatedPrice ?: 0.0) }
                        ?.currencyText(),
            )
        }
    }

    private fun save(
        onFailure: (String?) -> Unit,
        block: suspend () -> Unit,
    ) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { block() }.onFailure { error -> onFailure(error.message) }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun showError(message: String?) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun updateForm(transform: (ItemForm) -> ItemForm) {
        _uiState.update { state -> state.copy(itemForm = state.itemForm?.let(transform)) }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val MIN_QUERY_LENGTH = 2
        private const val MAX_ORIGIN_LENGTH = 20
        private const val MAX_NOTE_LENGTH = 255
        private const val DISCARD_REASON = "Rascunho descartado"

        fun factory(draftId: Int?): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    RequisitionEditorViewModel(
                        draftId = draftId,
                        requisitionRepository = application.container.requisitionRepository,
                        productRepository = application.container.productRepository,
                        supplierRepository = application.container.supplierRepository,
                        authRepository = application.container.authRepository,
                    )
                }
            }
    }
}

fun originLabel(type: RequisitionType): String = if (type == RequisitionType.TRANSFER) "Destino" else "Origem"

private fun missingOriginMessage(type: RequisitionType): String =
    if (type == RequisitionType.TRANSFER) {
        "Informe o destino antes de adicionar itens."
    } else {
        "Informe a origem antes de adicionar itens."
    }

private fun String.toNumberOrNull(): Double? = trim().replace(',', '.').toDoubleOrNull()

private fun RequisitionItemResponse.toUi(): EditorItemUi {
    val price = estimatedPrice?.let { "Preço est. ${it.currencyText()}" }
    val details =
        listOfNotNull(price, suggestedSupplier?.legalName, note?.takeIf { it.isNotBlank() })
            .joinToString(" · ")
            .ifEmpty { null }
    return EditorItemUi(
        id = id,
        name = product.name,
        quantityText = "Qtd. ${quantity.quantityText()}",
        details = details,
    )
}
