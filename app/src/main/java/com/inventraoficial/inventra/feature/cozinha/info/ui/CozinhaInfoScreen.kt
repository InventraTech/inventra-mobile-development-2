package com.inventraoficial.inventra.feature.cozinha.info.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
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
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraFilterChip
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.feature.cozinha.create.ui.EstablishmentType
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun CozinhaInfoScreen(
    name: String,
    onNameChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    establishmentType: EstablishmentType?,
    onTypeSelect: (EstablishmentType) -> Unit,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onBackClick: () -> Unit,
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
                InventraBackButton(onClick = onBackClick)
            },
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
                if (!isEditing) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color.White,
                        modifier = Modifier.clickable(onClick = onEditClick),
                    )
                }
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
                readOnly = !isEditing,
                placeholder = "Insira o nome da cozinha",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraLabeledField(
                label = "Endereço",
                value = address,
                onValueChange = onAddressChange,
                readOnly = !isEditing,
                placeholder = "Rua, número, bairro, cidade",
                modifier = Modifier.fillMaxWidth(),
            )
            if (isEditing) {
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
            } else {
                InventraLabeledField(
                    label = "Tipo de estabelecimento",
                    value = establishmentType?.label.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    placeholder = "",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (isEditing) {
                InventraPrimaryButton(
                    text = "Salvar",
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onSaveClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                InventraSecondaryButton(
                    text = "Cancelar",
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun CozinhaInfoRoute(
    navigator: Navigator,
    viewModel: CozinhaInfoViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    CozinhaInfoScreen(
        name = uiState.name,
        onNameChange = viewModel::onNameChange,
        address = uiState.address,
        onAddressChange = viewModel::onAddressChange,
        establishmentType = uiState.establishmentType,
        onTypeSelect = viewModel::onTypeSelect,
        isEditing = uiState.isEditing,
        onEditClick = viewModel::onEditClick,
        onSaveClick = viewModel::onSaveClick,
        onCancelClick = viewModel::onCancelClick,
        onBackClick = { navigator.back() },
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun CozinhaInfoScreenPreview() {
    CozinhaInfoScreen(
        name = "Sabor Filial Sul",
        onNameChange = {},
        address = "Av. das Indústrias, 450 – São Paulo, SP",
        onAddressChange = {},
        establishmentType = EstablishmentType.Filial,
        onTypeSelect = {},
        isEditing = false,
        onEditClick = {},
        onSaveClick = {},
        onCancelClick = {},
        onBackClick = {},
    )
}
