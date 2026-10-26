package com.inventraoficial.inventra.feature.cozinha.select.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraCloseButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraFilterChip
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRequestStatus
import com.inventraoficial.inventra.core.designsystem.molecules.InventraCozinhaRow
import com.inventraoficial.inventra.core.designsystem.molecules.InventraSearchWithFilter
import com.inventraoficial.inventra.core.designsystem.organisms.InventraCozinhaSheet
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.data.local.LOCATION_PERMISSIONS
import com.inventraoficial.inventra.data.local.hasLocationPermission
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraBrand
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSurface
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
private fun PendingRequestBanner(
    cozinhaName: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text =
            "Você tem um pedido pendente para $cozinhaName. Dá para ver as cozinhas, " +
                "mas pedir em outra só depois da resposta.",
        fontFamily = Montserrat,
        fontSize = 13.sp,
        color = InventraPurple,
        modifier =
            modifier
                .fillMaxWidth()
                .background(InventraGold.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(12.dp),
    )
}

@Composable
fun SelectCozinhaScreen(
    uiState: SelectCozinhaUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterIconClick: () -> Unit,
    onViewModeSelect: (CozinhaViewMode) -> Unit,
    onCozinhaClick: (CozinhaListItem) -> Unit,
    onRequestAccessClick: () -> Unit,
    onSheetDismiss: () -> Unit,
    onRetryClick: () -> Unit,
    onCloseClick: () -> Unit,
    onNearMeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    uiState.selectedCozinha?.let { cozinha ->
        InventraCozinhaSheet(
            name = cozinha.name,
            location = cozinha.location,
            distance = cozinha.distance,
            status = cozinha.status,
            onRequestClick = onRequestAccessClick,
            onDismiss = onSheetDismiss,
            isLoading = uiState.isRequesting,
            errorMessage = uiState.requestErrorMessage,
            blockedMessage =
                uiState.pendingCozinhaName
                    ?.takeIf { cozinha.status != InventraCozinhaRequestStatus.Pendente }
                    ?.let { "Você já tem um pedido pendente para $it. Aguarde a resposta antes de pedir em outra." },
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            navigationIcon = {
                InventraCloseButton(onClick = onCloseClick)
            },
            title = {
                Text(
                    "Selecionar cozinha",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
            },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(16.dp),
        ) {
            uiState.pendingCozinhaName?.let { name -> PendingRequestBanner(name, Modifier.padding(bottom = 12.dp)) }
            InventraSearchWithFilter(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                onFilterClick = onFilterIconClick,
                placeholder = "Buscar cozinha...",
                modifier = Modifier.padding(bottom = 12.dp),
            )
            NearMeRow(
                isNearMe = uiState.isNearMe,
                isLocating = uiState.isLocating,
                message = uiState.locationMessage,
                onClick = onNearMeClick,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            ViewModeToggle(
                selected = uiState.viewMode,
                onSelect = onViewModeSelect,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            when {
                uiState.isLoading && uiState.cozinhas.isEmpty() -> CenteredLoading(modifier = Modifier.weight(1f))
                uiState.errorMessage != null ->
                    ErrorState(
                        message = uiState.errorMessage,
                        onRetryClick = onRetryClick,
                        modifier = Modifier.weight(1f),
                    )
                uiState.viewMode == CozinhaViewMode.List ->
                    CozinhaList(
                        cozinhas = uiState.cozinhas,
                        isLoading = uiState.isLoading,
                        onCozinhaClick = onCozinhaClick,
                        modifier = Modifier.weight(1f),
                    )
                else ->
                    CozinhaMap(
                        cozinhas = uiState.cozinhas,
                        userLocation = uiState.userLocation.takeIf { uiState.isNearMe },
                        showMyLocation = uiState.hasLocationPermission,
                        onCozinhaClick = onCozinhaClick,
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                    )
            }
        }
    }
}

@Composable
private fun CozinhaList(
    cozinhas: List<CozinhaListItem>,
    isLoading: Boolean,
    onCozinhaClick: (CozinhaListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (isLoading) {
            LinearProgressIndicator(
                color = InventraPurple,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
            )
        }
        if (cozinhas.isEmpty()) {
            Text(
                "Nenhuma cozinha encontrada.",
                color = InventraMuted,
                fontSize = 14.sp,
                fontFamily = Montserrat,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cozinhas, key = { it.id }) { cozinha ->
                    InventraCozinhaRow(
                        name = cozinha.name,
                        subtitle = cozinha.subtitle,
                        status = cozinha.status,
                        onClick = { onCozinhaClick(cozinha) },
                        modifier = Modifier.fillMaxWidth(),
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
        Text(
            message,
            fontSize = 15.sp,
            fontFamily = Montserrat,
            textAlign = TextAlign.Center,
        )
        InventraSecondaryButton(text = "Tentar novamente", onClick = onRetryClick)
    }
}

private const val TOGGLE_CORNER_PERCENT = 50

@Composable
private fun ViewModeToggle(
    selected: CozinhaViewMode,
    onSelect: (CozinhaViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val toggleShape = RoundedCornerShape(percent = TOGGLE_CORNER_PERCENT)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), toggleShape),
    ) {
        CozinhaViewMode.entries.forEach { mode ->
            val isSelected = mode == selected
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .background(if (isSelected) InventraBrand else InventraSurface, toggleShape)
                        .clickable(onClick = { onSelect(mode) })
                        .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (mode == CozinhaViewMode.List) "Lista" else "Mapa",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else InventraPurple,
                )
            }
        }
    }
}

@Composable
private fun NearMeRow(
    isNearMe: Boolean,
    isLocating: Boolean,
    message: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InventraFilterChip(
                label = if (isNearMe) "Perto de mim (até 50 km)" else "Perto de mim",
                selected = isNearMe,
                onClick = onClick,
            )
            if (isLocating) {
                CircularProgressIndicator(
                    color = InventraPurple,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        if (message != null) {
            Text(message, color = InventraMuted, fontSize = 12.sp, fontFamily = Montserrat)
        }
    }
}

@Composable
fun SelectCozinhaRoute(
    navigator: Navigator,
    viewModel: SelectCozinhaViewModel = viewModel(factory = SelectCozinhaViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            viewModel.onLocationPermissionResult(result.values.any { it })
        }

    LaunchedEffect(Unit) {
        viewModel.onLocationPermissionChecked(context.hasLocationPermission())
    }

    LaunchedEffect(uiState.isRequestSent) {
        if (uiState.isRequestSent) navigator.replaceStack(Screen.RequestSent)
    }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) navigator.replaceStack(Screen.Login)
    }

    SelectCozinhaScreen(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterIconClick = viewModel::onFilterIconClick,
        onViewModeSelect = viewModel::onViewModeSelect,
        onCozinhaClick = viewModel::onCozinhaClick,
        onRequestAccessClick = viewModel::onRequestAccessClick,
        onSheetDismiss = viewModel::onSheetDismiss,
        onRetryClick = viewModel::onRetryClick,
        // Aberta por cima de "Minhas solicitações", fechar volta para lá; como primeira tela, faz logout.
        onCloseClick = { if (navigator.canGoBack) navigator.back() else viewModel.onCloseClick() },
        onNearMeClick = {
            if (uiState.isNearMe || uiState.hasLocationPermission) {
                viewModel.onNearMeClick()
            } else {
                locationPermission.launch(LOCATION_PERMISSIONS)
            }
        },
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun SelectCozinhaScreenPreview() {
    SelectCozinhaScreen(
        uiState =
            SelectCozinhaUiState(
                cozinhas =
                    listOf(
                        CozinhaListItem(
                            id = 1,
                            name = "Restaurante Sabor Caseiro",
                            location = "São Paulo, SP",
                            distance = "3,2 km",
                            status = InventraCozinhaRequestStatus.Disponivel,
                        ),
                        CozinhaListItem(
                            id = 2,
                            name = "Tung Tung Tung Sahur",
                            location = "Rio de Janeiro, RJ",
                            distance = null,
                            status = InventraCozinhaRequestStatus.Pendente,
                        ),
                        CozinhaListItem(
                            id = 3,
                            name = "Instituto J&F",
                            location = null,
                            distance = null,
                            status = InventraCozinhaRequestStatus.Recusada,
                        ),
                    ),
            ),
        onSearchQueryChange = {},
        onFilterIconClick = {},
        onViewModeSelect = {},
        onCozinhaClick = {},
        onRequestAccessClick = {},
        onSheetDismiss = {},
        onRetryClick = {},
        onCloseClick = {},
        onNearMeClick = {},
    )
}
