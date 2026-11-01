package com.inventraoficial.inventra.feature.notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraStatusIconVariant
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.core.designsystem.organisms.InventraFilterChipRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraGroupedNotifications
import com.inventraoficial.inventra.core.designsystem.organisms.InventraNotification
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScreenContent
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat
import java.time.LocalDate

@Composable
fun NotificationScreen(
    groupedNotifications: Map<String, List<InventraNotification>>,
    isHistoryExpanded: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onNotificationClick: (InventraNotification) -> Unit,
    onSeeAllClick: () -> Unit,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedFilter: NotificationFilter = NotificationFilter.All,
    hasUnread: Boolean = false,
    onFilterSelect: (NotificationFilter) -> Unit = {},
    onMarkAllReadClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            navigationIcon = {
                InventraBackButton(onClick = onBackClick)
            },
            title = {
                Text(
                    "Notificações",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontSize = 17.sp,
                )
            },
        )
        InventraScreenContent {
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
                    ) {
                        InventraFilterChipRow(
                            options = NotificationFilter.entries.map { it.label },
                            selected = selectedFilter.label,
                            onSelect = { label ->
                                onFilterSelect(NotificationFilter.entries.first { it.label == label })
                            },
                        )
                        if (hasUnread) {
                            InventraTextLink(
                                text = "Marcar todas como lidas",
                                onClick = onMarkAllReadClick,
                                modifier =
                                    Modifier
                                        .padding(top = 12.dp)
                                        .align(Alignment.End),
                            )
                        }
                        if (groupedNotifications.isEmpty()) {
                            Text(
                                text =
                                    if (isHistoryExpanded) "Nenhuma notificação." else "Nenhuma notificação recente.",
                                fontFamily = Montserrat,
                                fontSize = 14.sp,
                                color = InventraMuted,
                                modifier = Modifier.padding(vertical = 12.dp),
                            )
                        }
                        InventraGroupedNotifications(
                            groups = groupedNotifications,
                            onNotificationClick = onNotificationClick,
                        )
                        InventraTextLink(
                            text = if (isHistoryExpanded) "Ver menos" else "Ver todas",
                            onClick = onSeeAllClick,
                            showArrow = !isHistoryExpanded,
                            modifier =
                                Modifier
                                    .padding(top = 12.dp)
                                    .align(Alignment.End),
                        )
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
fun NotificationRoute(
    navigator: Navigator,
    viewModel: NotificationViewModel = viewModel(factory = NotificationViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onScreenVisible()
    }

    NotificationScreen(
        groupedNotifications = uiState.groupedNotifications,
        isHistoryExpanded = uiState.isHistoryExpanded,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onNotificationClick = { notification ->
            viewModel.onNotificationClick(notification)
            uiState.targets[notification.id]?.let { target -> navigator.navigate(target.toScreen()) }
        },
        onSeeAllClick = viewModel::onSeeAllClick,
        onRetryClick = viewModel::onRetryClick,
        onBackClick = { navigator.back() },
        modifier = modifier,
        selectedFilter = uiState.selectedFilter,
        hasUnread = uiState.hasUnread,
        onFilterSelect = viewModel::onFilterSelect,
        onMarkAllReadClick = viewModel::onMarkAllReadClick,
    )
}

private fun NotificationTarget.toScreen(): Screen =
    when (this) {
        is NotificationTarget.Product -> Screen.StockDetail(productId)
        is NotificationTarget.Requisition -> Screen.RequisitionDetail(requisitionId)
        NotificationTarget.Members -> Screen.ManageMembers
        is NotificationTarget.NewProduct -> Screen.ProductCreate(barcode)
    }

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun NotificationScreenPreview() {
    NotificationScreen(
        groupedNotifications =
            mapOf(
                "Hoje" to
                    listOf(
                        InventraNotification(
                            variant = InventraStatusIconVariant.Danger,
                            title = "Leite Integral venceu",
                            subtitle = "Lote QA-1011-01",
                            timestamp = "08:30",
                            date = LocalDate.now(),
                            id = 1,
                            isUnread = true,
                        ),
                        InventraNotification(
                            variant = InventraStatusIconVariant.Danger,
                            title = "Óleo de Soja abaixo do mínimo",
                            subtitle = "Reponha o estoque",
                            timestamp = "07:15",
                            date = LocalDate.now(),
                            id = 2,
                        ),
                    ),
            ),
        isHistoryExpanded = false,
        isLoading = false,
        errorMessage = null,
        hasUnread = true,
        onNotificationClick = {},
        onSeeAllClick = {},
        onRetryClick = {},
        onBackClick = {},
    )
}
