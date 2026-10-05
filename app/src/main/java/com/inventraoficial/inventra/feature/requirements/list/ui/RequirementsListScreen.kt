package com.inventraoficial.inventra.feature.requirements.list.ui

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

@Composable
fun RequirementsListScreen(
    requirements: List<Requirement>,
    onAcceptRequirement: (Requirement) -> Unit,
    onRejectRequirement: (Requirement) -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
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
                .padding(16.dp)
                .height(56.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tabs.forEachIndexed { index, tab ->
                FilterChip(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        if (index == 1) onNavigateToHistory()
                    },
                    label = {
                        Text(
                            text = tab,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
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
                        .height(56.dp),
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(16.dp),
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(requirements) { requirement ->
                    RequirementAcceptanceCard(
                        painter = painterResource(requirement.imageRes),
                        title = requirement.productName,
                        description = requirement.description,
                        onAccept = { onAcceptRequirement(requirement) },
                        onReject = { onRejectRequirement(requirement) },
                    )
                }
            }
        }
    }
}

@Composable
fun RequirementsListRoute(
    navigator: Navigator,
    viewModel: RequirementsListViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    RequirementsListScreen(
        requirements = uiState.requirements,
        onAcceptRequirement = viewModel::onAcceptRequirement,
        onRejectRequirement = viewModel::onRejectRequirement,
        onNavigateToHistory = { navigator.navigate(Screen.RequirementsHistory) },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun RequirementsListScreenPreview() {
    RequirementsListScreen(
        requirements = emptyList(),
        onAcceptRequirement = {},
        onRejectRequirement = {},
        onNavigateToHistory = {},
    )
}
