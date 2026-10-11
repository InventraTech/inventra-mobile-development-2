package com.inventraoficial.inventra.feature.cozinha.create.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun CreateCozinhaScreen(
    name: String,
    onNameChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    onCloseClick: () -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
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
                    .imePadding()
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
                isLoading = isLoading,
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
    viewModel: CreateCozinhaViewModel = viewModel(factory = CreateCozinhaViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.isCreated) {
        if (uiState.isCreated) navigator.navigateTopLevel(Screen.Home)
    }
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) navigator.replaceStack(Screen.Login)
    }

    CreateCozinhaScreen(
        name = uiState.name,
        onNameChange = viewModel::onNameChange,
        address = uiState.address,
        onAddressChange = viewModel::onAddressChange,
        onCreateClick = viewModel::onCreateClick,
        onCloseClick = viewModel::onCloseClick,
        errorMessage = uiState.errorMessage,
        isLoading = uiState.isLoading,
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
        onCreateClick = {},
        onCloseClick = {},
        errorMessage = null,
        isLoading = false,
    )
}
