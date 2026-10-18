package com.inventraoficial.inventra.feature.cozinha.info.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraSwitchRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.data.local.LOCATION_PERMISSIONS
import com.inventraoficial.inventra.data.local.hasLocationPermission
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun CozinhaInfoScreen(
    uiState: CozinhaInfoUiState,
    actions: CozinhaInfoActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            navigationIcon = { InventraBackButton(onClick = actions.onBackClick) },
            title = {
                Text(
                    "Informações da cozinha",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
            },
            actions = {
                if (uiState.showEditButton) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color.White,
                        modifier = Modifier.clickable(onClick = actions.onEditClick),
                    )
                }
            },
        )
        val contentModifier = Modifier.weight(1f).fillMaxWidth()
        when {
            uiState.isLoading ->
                Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = InventraPurple)
                }
            uiState.errorMessage != null ->
                Column(
                    modifier = contentModifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(uiState.errorMessage, fontFamily = Montserrat, fontSize = 15.sp, textAlign = TextAlign.Center)
                    InventraSecondaryButton(text = "Tentar novamente", onClick = actions.onRetryClick)
                }
            else -> CozinhaInfoForm(uiState = uiState, actions = actions, modifier = contentModifier)
        }
    }
}

@Composable
private fun CozinhaInfoForm(
    uiState: CozinhaInfoUiState,
    actions: CozinhaInfoActions,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val readOnly = !uiState.isEditing

    Column(
        modifier =
            modifier
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        uiState.savedMessage?.let { Banner(text = it, color = InventraSuccess) }
        InventraLabeledField(
            label = "Nome da cozinha",
            value = uiState.name,
            onValueChange = actions.onNameChange,
            readOnly = readOnly,
            placeholder = "Insira o nome da cozinha",
            modifier = Modifier.fillMaxWidth(),
        )
        InventraLabeledField(
            label = "Código de convite",
            value = uiState.code,
            onValueChange = {},
            readOnly = true,
            placeholder = "",
            modifier = Modifier.fillMaxWidth(),
        )
        InventraLabeledField(
            label = "Endereço",
            value = uiState.address,
            onValueChange = actions.onAddressChange,
            readOnly = readOnly,
            placeholder = "Rua e número",
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InventraLabeledField(
                label = "Cidade",
                value = uiState.city,
                onValueChange = actions.onCityChange,
                readOnly = readOnly,
                placeholder = "Ex.: São Paulo",
                modifier = Modifier.weight(2f),
            )
            InventraLabeledField(
                label = "UF",
                value = uiState.state,
                onValueChange = actions.onStateChange,
                readOnly = readOnly,
                placeholder = "SP",
                modifier = Modifier.weight(1f),
            )
        }
        LocationSection(uiState = uiState, onUseMyLocationClick = actions.onUseMyLocationClick)
        if (uiState.isEditing) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                InventraSwitchRow(
                    label = "Aceitar pedidos de entrada",
                    checked = uiState.acceptingRequests,
                    onCheckedChange = actions.onAcceptingRequestsChange,
                )
                Text(
                    "Desligado, a cozinha some da busca e só entra quem tiver o código de convite.",
                    fontFamily = Montserrat,
                    fontSize = 13.sp,
                    color = InventraMuted,
                )
            }
        } else {
            InventraLabeledField(
                label = "Pedidos de entrada",
                value = if (uiState.acceptingRequests) ACCEPTING else INVITE_ONLY,
                onValueChange = {},
                readOnly = true,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (uiState.isEditing) {
            uiState.saveError?.let { Text(it, fontFamily = Montserrat, fontSize = 14.sp, color = InventraDanger) }
            InventraPrimaryButton(
                text = "Salvar",
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    actions.onSaveClick()
                },
                isLoading = uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
            InventraSecondaryButton(
                text = "Cancelar",
                onClick = actions.onCancelClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LocationSection(
    uiState: CozinhaInfoUiState,
    onUseMyLocationClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Localização no mapa", fontFamily = Montserrat, fontSize = 18.sp, color = InventraPurple)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                Icons.Default.Place,
                contentDescription = null,
                tint = if (uiState.hasLocation || uiState.newLocation != null) InventraPurple else InventraMuted,
            )
            Text(uiState.locationStatus, fontFamily = Montserrat, fontSize = 14.sp)
        }
        if (uiState.isEditing) {
            Text(
                "Ao mudar o endereço, o pino é buscado por ele. Se estiver na cozinha, use a sua localização.",
                fontFamily = Montserrat,
                fontSize = 13.sp,
                color = InventraMuted,
            )
            if (uiState.isLocating) {
                CircularProgressIndicator(color = InventraPurple, modifier = Modifier.padding(4.dp))
            } else {
                InventraSecondaryButton(
                    text = "Usar minha localização atual",
                    onClick = onUseMyLocationClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            uiState.locationMessage?.let { Text(it, fontFamily = Montserrat, fontSize = 13.sp, color = InventraDanger) }
        }
    }
}

@Composable
private fun Banner(
    text: String,
    color: Color,
) {
    Text(
        text,
        fontFamily = Montserrat,
        fontSize = 13.sp,
        color = color,
        modifier =
            Modifier
                .fillMaxWidth()
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(12.dp),
    )
}

@Composable
fun CozinhaInfoRoute(
    navigator: Navigator,
    viewModel: CozinhaInfoViewModel = viewModel(factory = CozinhaInfoViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            viewModel.onLocationPermissionResult(result.values.any { it })
        }

    CozinhaInfoScreen(
        uiState = uiState,
        actions =
            CozinhaInfoActions(
                onNameChange = viewModel::onNameChange,
                onAddressChange = viewModel::onAddressChange,
                onCityChange = viewModel::onCityChange,
                onStateChange = viewModel::onStateChange,
                onAcceptingRequestsChange = viewModel::onAcceptingRequestsChange,
                onUseMyLocationClick = {
                    if (context.hasLocationPermission()) {
                        viewModel.onUseMyLocationClick()
                    } else {
                        locationPermission.launch(LOCATION_PERMISSIONS)
                    }
                },
                onEditClick = viewModel::onEditClick,
                onSaveClick = viewModel::onSaveClick,
                onCancelClick = viewModel::onCancelClick,
                onRetryClick = viewModel::onRetryClick,
                onBackClick = { navigator.back() },
            ),
        modifier = modifier,
    )
}

private const val ACCEPTING = "Aparece na busca e aceita pedidos"
private const val INVITE_ONLY = "Só com o código de convite"

private val previewActions =
    CozinhaInfoActions(
        onNameChange = {},
        onAddressChange = {},
        onCityChange = {},
        onStateChange = {},
        onAcceptingRequestsChange = {},
        onUseMyLocationClick = {},
        onEditClick = {},
        onSaveClick = {},
        onCancelClick = {},
        onRetryClick = {},
        onBackClick = {},
    )

private val previewState =
    CozinhaInfoUiState(
        isLoading = false,
        canEdit = true,
        code = "K7X2PQ",
        name = "Sabor Filial Sul",
        address = "Av. das Indústrias, 450",
        city = "São Paulo",
        state = "SP",
        hasLocation = true,
    )

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun CozinhaInfoScreenPreview() {
    CozinhaInfoScreen(uiState = previewState, actions = previewActions)
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun CozinhaInfoEditingPreview() {
    CozinhaInfoScreen(uiState = previewState.copy(isEditing = true, hasLocation = false), actions = previewActions)
}
