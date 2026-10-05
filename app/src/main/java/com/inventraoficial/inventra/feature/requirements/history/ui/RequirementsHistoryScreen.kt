package com.inventraoficial.inventra.feature.requirements.history.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

private const val TAB_HEIGHT = 56
private const val TAB_FONT_SIZE = 18
private const val PADDING_HORIZONTAL = 16
private const val SPACING_BETWEEN_ITEMS = 12

@Composable
fun RequirementsHistoryScreen(
    historyItems: List<HistoryItem>,
    onNavigateToRequirements: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by remember { mutableIntStateOf(1) }
    val tabs = listOf("Requisições", "Histórico")

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        InventraTopBar(
            title = {
                Text(
                    "Requisições",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontSize = 17.sp,
                )
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PADDING_HORIZONTAL.dp)
                .height(TAB_HEIGHT.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tabs.forEachIndexed { index, tab ->
                FilterChip(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        if (index == 0) onNavigateToRequirements()
                    },
                    label = {
                        Text(
                            text = tab,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = TAB_FONT_SIZE.sp,
                        )
                    },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = InventraPurple,
                            selectedLabelColor = Color.White,
                            labelColor = InventraPurple,
                        ),
                    modifier = Modifier
                        .weight(1f)
                        .height(TAB_HEIGHT.dp),
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(PADDING_HORIZONTAL.dp),
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(SPACING_BETWEEN_ITEMS.dp),
            ) {
                items(historyItems) { item ->
                    HistoryItemCard(
                        painter = painterResource(item.imageRes),
                        title = item.productName,
                        description = item.description,
                        status = item.status,
                    )
                }
            }
        }
    }
}

@Composable
fun RequirementsHistoryRoute(
    navigator: Navigator,
    viewModel: RequirementsHistoryViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    RequirementsHistoryScreen(
        historyItems = uiState.historyItems,
        onNavigateToRequirements = { navigator.navigate(Screen.RequirementsList) },
        modifier = modifier,
    )
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun RequirementsHistoryScreenPreview() {
    RequirementsHistoryScreen(
        historyItems = emptyList(),
        onNavigateToRequirements = {},
    )
}
