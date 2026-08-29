package com.inventraoficial.inventra.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.molecules.InventraStatTone
import com.inventraoficial.inventra.core.designsystem.molecules.InventraUrgency
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomBar
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomDestination
import com.inventraoficial.inventra.core.designsystem.organisms.InventraPriorityAlertsSection
import com.inventraoficial.inventra.core.designsystem.organisms.InventraPriorityItem
import com.inventraoficial.inventra.core.designsystem.organisms.InventraQrScanFab
import com.inventraoficial.inventra.core.designsystem.organisms.InventraStat
import com.inventraoficial.inventra.core.designsystem.organisms.InventraStatGrid
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun HomeScreen(
    userName: String,
    dateText: String,
    priorityItems: List<InventraPriorityItem>,
    stats: List<InventraStat>,
    onQrScanClick: () -> Unit,
    onBellClick: () -> Unit,
    onStockClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.White),
        ) {
            InventraTopBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Seja bem-vindo, $userName!",
                            color = Color.White,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                        Text(
                            dateText,
                            color = Color.White.copy(alpha = 0.8f),
                            fontFamily = Montserrat,
                            fontSize = 12.sp,
                        )
                    }
                },
                actions = {
                    Text("🔔", fontSize = 20.sp, modifier = Modifier.clickable(onClick = onBellClick))
                },
            )
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                InventraPriorityAlertsSection(items = priorityItems)
                Text(
                    text = "Dados do Mês",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 20.dp),
                    color = InventraPurple,
                )
                InventraStatGrid(stats = stats, modifier = Modifier.padding(top = 20.dp))
            }
            InventraBottomBar(
                selected = InventraBottomDestination.Home,
                onSelect = {},
            )
        }
        InventraQrScanFab(
            onClick = onQrScanClick,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp)
                    .offset(y = (-90).dp),
        )
    }
}

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreen(
        userName = uiState.userName,
        dateText = uiState.dateText,
        priorityItems = uiState.priorityItems,
        stats = uiState.stats,
        onQrScanClick = viewModel::onQrScanClick,
        onBellClick = viewModel::onBellClick,
        onStockClick = viewModel::onStockClick,
        onHistoryClick = viewModel::onHistoryClick,
        onNotificationsClick = viewModel::onNotificationsClick,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        userName = "Jones",
        dateText = "Quinta, 2 de julho de 2026",
        priorityItems =
            listOf(
                InventraPriorityItem(
                    imageRes = R.drawable.ic_inventra_logo,
                    title = "Arroz Camil",
                    statusText = "Vence em 2 dias",
                    urgency = InventraUrgency.Normal,
                    batch = "Lote 0001",
                ),
                InventraPriorityItem(
                    imageRes = R.drawable.ic_inventra_logo,
                    title = "Arroz Camil",
                    statusText = "Vence em 2 dias",
                    urgency = InventraUrgency.Normal,
                    batch = "Lote 0001",
                ),
            ),
        stats =
            listOf(
                InventraStat("Economia", "R$ 6767,00", "↑ 67%", InventraStatTone.Positive),
                InventraStat("Perdas", "R$ 6767,00", "↓ 67%", InventraStatTone.Negative),
                InventraStat("Perdas", "R$ 6767,00", "↓ 67%", InventraStatTone.Negative),
                InventraStat("Economia", "R$ 6767,00", "↑ 67%", InventraStatTone.Positive),
            ),
        onQrScanClick = {},
        onBellClick = {},
        onStockClick = {},
        onHistoryClick = {},
        onNotificationsClick = {},
    )
}
