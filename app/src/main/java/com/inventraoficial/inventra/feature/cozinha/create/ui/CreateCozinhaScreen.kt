package com.inventraoficial.inventra.feature.cozinha.create.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraCloseButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraFilterChip
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun CreateCozinhaScreen(
    name: String,
    onNameChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    establishmentType: EstablishmentType?,
    onTypeSelect: (EstablishmentType) -> Unit,
    onCreateClick: () -> Unit,
    onCloseClick: () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        InventraTopBar(
            navigationIcon = {
                InventraCloseButton(onClick = onCloseClick)
            },
            title = {
                Text(
                    "Criar cozinha",
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            InventraLabeledField(
                label = "Nome da cozinha",
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Insira o nome da cozinha",
            )
            InventraLabeledField(
                label = "Endereço",
                value = address,
                onValueChange = onAddressChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Rua, número, bairro, cidade",
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Tipo de estabelecimento",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = InventraPurple,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstablishmentType.entries.forEach { type ->
                        InventraFilterChip(
                            label = type.label,
                            selected = type == establishmentType,
                            onClick = { onTypeSelect(type) },
                        )
                    }
                }
            }
            Text(
                text =
                    "Cadastre o estabelecimento que você vai gerenciar. " +
                        "Você poderá convidar sua equipe assim que a cozinha for criada.",
                fontFamily = Montserrat,
                fontSize = 13.sp,
                color = Color.Gray,
            )
            Text(
                text = errorMessage.orEmpty(),
                color = InventraDanger,
                minLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            InventraPrimaryButton(
                text = "Criar cozinha",
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onCreateClick()
                },
                modifier = Modifier.fillMaxWidth(),
            )
            InventraSecondaryButton(
                text = "Cancelar",
                onClick = onCloseClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun CreateCozinhaRoute(
    navigator: Navigator,
    viewModel: CreateCozinhaViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    CreateCozinhaScreen(
        name = uiState.name,
        onNameChange = viewModel::onNameChange,
        address = uiState.address,
        onAddressChange = viewModel::onAddressChange,
        establishmentType = uiState.establishmentType,
        onTypeSelect = viewModel::onTypeSelect,
        onCreateClick = {
            if (viewModel.onCreateClick()) {
                navigator.navigateTopLevel(Screen.Home)
            }
        },
        onCloseClick = { navigator.replaceStack(Screen.Login) },
        errorMessage = uiState.errorMessage,
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun CreateCozinhaScreenPreview() {
    CreateCozinhaScreen(
        name = "",
        onNameChange = {},
        address = "",
        onAddressChange = {},
        establishmentType = EstablishmentType.Restaurante,
        onTypeSelect = {},
        onCreateClick = {},
        onCloseClick = {},
        errorMessage = null,
    )
}
