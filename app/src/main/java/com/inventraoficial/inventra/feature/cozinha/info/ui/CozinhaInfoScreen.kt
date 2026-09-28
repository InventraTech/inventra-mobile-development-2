package com.inventraoficial.inventra.feature.cozinha.info.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun CozinhaInfoScreen(
    name: String,
    address: String,
    establishmentType: String,
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
                    "Informações da cozinha",
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
                onValueChange = {},
                readOnly = true,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraLabeledField(
                label = "Endereço",
                value = address,
                onValueChange = {},
                readOnly = true,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraLabeledField(
                label = "Tipo de estabelecimento",
                value = establishmentType,
                onValueChange = {},
                readOnly = true,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
            )
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
        address = uiState.address,
        establishmentType = uiState.establishmentType,
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
        address = "Av. das Indústrias, 450 – São Paulo, SP",
        establishmentType = "Filial",
        onBackClick = {},
    )
}
