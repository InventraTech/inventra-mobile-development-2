package com.inventraoficial.inventra.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraStatTone
import com.inventraoficial.inventra.core.designsystem.molecules.InventraUrgency
import com.inventraoficial.inventra.core.designsystem.organisms.InventraPriorityAlertsSection
import com.inventraoficial.inventra.core.designsystem.organisms.InventraPriorityItem
import com.inventraoficial.inventra.core.designsystem.organisms.InventraStat
import com.inventraoficial.inventra.core.designsystem.organisms.InventraStatGrid
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.feature.notifications.RequestNotificationPermissionOnStart
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

private const val MAX_BADGE = 9

@Composable
fun HomeScreen(
    userName: String,
    dateText: String,
    priorityItems: List<InventraPriorityItem>,
    stats: List<InventraStat>,
    isLoading: Boolean,
    errorMessage: String?,
    unreadNotifications: Int,
    onBellClick: () -> Unit,
    onPriorityItemClick: (InventraPriorityItem) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
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
                BadgedBox(
                    badge = {
                        if (unreadNotifications > 0) {
                            val label = if (unreadNotifications > MAX_BADGE) "$MAX_BADGE+" else "$unreadNotifications"
                            Badge { Text(label) }
                        }
                    },
                    modifier = Modifier.clickable(onClick = onBellClick),
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notificações",
                        tint = Color.White,
                    )
                }
            },
        )
        when {
            isLoading -> CenteredLoading(modifier = Modifier.weight(1f))
            errorMessage != null ->
                ErrorState(message = errorMessage, onRetryClick = onRetryClick, modifier = Modifier.weight(1f))
            else ->
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    InventraPriorityAlertsSection(items = priorityItems, onItemClick = onPriorityItemClick)
                    if (priorityItems.isEmpty()) {
                        Text(
                            text = "Nenhum lote vence nos próximos ${HomeViewModel.EXPIRING_DAYS} dias.",
                            fontFamily = Montserrat,
                            fontSize = 14.sp,
                            color = InventraPurple.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                    if (stats.isNotEmpty()) {
                        Text(
                            text = "Resumo do estoque",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 20.dp),
                            color = InventraPurple,
                        )
                        InventraStatGrid(stats = stats, modifier = Modifier.padding(top = 20.dp))
                    }
                }
        }
    }
}

@Composable
private fun CenteredLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = InventraPurple)
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, fontFamily = Montserrat, fontSize = 15.sp, textAlign = TextAlign.Center)
        InventraSecondaryButton(text = "Tentar novamente", onClick = onRetryClick)
    }
}

@Composable
fun HomeRoute(
    navigator: Navigator,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onScreenVisible()
    }
    RequestNotificationPermissionOnStart()

    HomeScreen(
        userName = uiState.userName,
        dateText = uiState.dateText,
        priorityItems = uiState.priorityItems,
        stats = uiState.stats,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        unreadNotifications = uiState.unreadNotifications,
        onBellClick = { navigator.navigate(Screen.Notifications) },
        onPriorityItemClick = { item -> item.productId?.let { navigator.navigate(Screen.StockDetail(it)) } },
        onRetryClick = viewModel::onRetryClick,
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
        isLoading = false,
        errorMessage = null,
        unreadNotifications = 3,
        onBellClick = {},
        onPriorityItemClick = {},
        onRetryClick = {},
    )
}
