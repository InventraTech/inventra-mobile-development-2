package com.inventraoficial.inventra.feature.cozinha.select.ui

import androidx.lifecycle.ViewModel
import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRequestStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SelectCozinhaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SelectCozinhaUiState())
    val uiState: StateFlow<SelectCozinhaUiState> = _uiState.asStateFlow()

    // simula o formato que a API deve devolver no futuro
    private val allCozinhas: List<CozinhaListItem> = buildFakeCozinhas()

    init {
        refreshVisibleCozinhas()
    }

    private fun buildFakeCozinhas(): List<CozinhaListItem> =
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
            CozinhaListItem(
                name = "Instituto J&F",
                subtitle = "Matriz · Curitiba, PR",
                status = InventraCozinhaRequestStatus.Pendente,
            ),
            CozinhaListItem(
                name = "Restaurante Feijão Com Farinha",
                subtitle = "Filial · Manaus, AM",
                status = InventraCozinhaRequestStatus.Disponivel,
            ),
        )

    private fun refreshVisibleCozinhas() {
        val query = _uiState.value.searchQuery
        val visibleCozinhas = allCozinhas.filter { it.name.contains(query, ignoreCase = true) }
        _uiState.value = _uiState.value.copy(cozinhas = visibleCozinhas)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        refreshVisibleCozinhas()
    }

    fun onFilterIconClick() {
        // sem comportamento definido ainda
    }

    fun onViewModeSelect(viewMode: CozinhaViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = viewMode)
    }
}
