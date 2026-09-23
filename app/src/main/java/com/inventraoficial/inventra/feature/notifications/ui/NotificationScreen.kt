package com.inventraoficial.inventra.feature.notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraStatusIconVariant
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.core.designsystem.organisms.InventraFilterChipRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraGroupedNotifications
import com.inventraoficial.inventra.core.designsystem.organisms.InventraNotification
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.Montserrat
import java.time.LocalDate

@Composable
fun NotificationScreen(
    filterOptions: List<String>,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    groupedNotifications: Map<String, List<InventraNotification>>,
    isHistoryExpanded: Boolean,
    onSeeAllClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
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
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            InventraFilterChipRow(
                options = filterOptions,
                selected = selectedFilter,
                onSelect = onFilterSelect,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            InventraGroupedNotifications(
                groups = groupedNotifications,
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

@Composable
fun NotificationRoute(
    navigator: Navigator,
    viewModel: NotificationViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    NotificationScreen(
        filterOptions = uiState.filterOptions,
        selectedFilter = uiState.selectedFilter,
        onFilterSelect = viewModel::onFilterSelect,
        groupedNotifications = uiState.groupedNotifications,
        isHistoryExpanded = uiState.isHistoryExpanded,
        onSeeAllClick = viewModel::onSeeAllClick,
        onBackClick = { navigator.back() },
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun NotificationScreenPreview() {
    NotificationScreen(
        filterOptions = listOf("Todos", "Alertas", "Sistema"),
        selectedFilter = "Todos",
        onFilterSelect = {},
        groupedNotifications =
            mapOf(
                "Hoje" to
                    listOf(
                        InventraNotification(
                            variant = InventraStatusIconVariant.Danger,
                            title = "Leite integral vence amanhã!",
                            subtitle = "Lote 000067",
                            timestamp = "08:30",
                            date = LocalDate.now(),
                        ),
                        InventraNotification(
                            variant = InventraStatusIconVariant.Info,
                            title = "Novo fornecedor cadastrado",
                            subtitle = "Seara",
                            timestamp = "07:15",
                            date = LocalDate.now(),
                        ),
                    ),
                "Ontem" to
                    listOf(
                        InventraNotification(
                            variant = InventraStatusIconVariant.Success,
                            title = "Entrada de estoque confirmada",
                            subtitle = "Lote 000042",
                            timestamp = "18:20",
                            date = LocalDate.now().minusDays(1),
                        ),
                    ),
            ),
        isHistoryExpanded = false,
        onSeeAllClick = {},
        onBackClick = {},
    )
}
