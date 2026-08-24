package com.example.inventra_mobile_2.feature.home.ui

import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModel
import com.example.inventra_mobile_2.R
import com.example.inventra_mobile_2.core.designsystem.molecules.InventraStatTone
import com.example.inventra_mobile_2.core.designsystem.molecules.InventraUrgency
import com.example.inventra_mobile_2.core.designsystem.organisms.InventraPriorityItem
import com.example.inventra_mobile_2.core.designsystem.organisms.InventraStat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        _uiState.value = _uiState.value.copy(
            userName = "Jones",
            dateText = "Quinta, 2 de julho de 2026",
            priorityItems = listOf(
                InventraPriorityItem(
                    imageRes = R.drawable.ic_inventra_logo,
                    title = "Arroz Camil",
                    statusText = "Vence em 2 dias",
                    urgency = InventraUrgency.Normal,
                    batch = "Lote 0001"
                ),
                InventraPriorityItem(
                    imageRes = R.drawable.ic_inventra_logo,
                    title = "Leite Integral",
                    statusText = "Vence amanhã",
                    urgency = InventraUrgency.Tomorrow,
                    batch = "Lote 0001"
                ),
                InventraPriorityItem(
                    imageRes = R.drawable.ic_inventra_logo,
                    title = "Arroz Camil",
                    statusText = "Vence em 2 dias",
                    urgency = InventraUrgency.Normal,
                    batch = "Lote 0001"
                )
            ),
            stats = listOf(
                InventraStat("Economia", "R$ 6767,00", "↑ 67% vs mês anterior", InventraStatTone.Positive),
                InventraStat("Perdas", "R$ 6767,00", "↓ 67% vs mês anterior", InventraStatTone.Negative),
                InventraStat("Economia", "R$ 6767,00", "↑ 67% vs mês anterior", InventraStatTone.Positive),
                InventraStat("Perdas", "R$ 6767,00", "↓ 67% vs mês anterior", InventraStatTone.Negative)
            )
        )
    }

    fun onQrScanClick() {
        // sem navegação configurada ainda
    }

    fun onBellClick() {
    }

    fun onStockClick() {
    }

    fun onHistoryClick() {
    }

    fun onNotificationsClick() {
    }
}