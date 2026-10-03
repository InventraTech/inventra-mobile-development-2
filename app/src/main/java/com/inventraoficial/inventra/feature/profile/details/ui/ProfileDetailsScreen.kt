package com.inventraoficial.inventra.feature.profile.details.ui

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
fun ProfileDetailsScreen(
    fullName: String,
    login: String,
    roleLabel: String,
    cozinhaName: String,
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
                    "Dados do perfil",
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
                label = "Nome completo",
                value = fullName,
                onValueChange = {},
                readOnly = true,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraLabeledField(
                label = "Login",
                value = login,
                onValueChange = {},
                readOnly = true,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraLabeledField(
                label = "Cargo",
                value = roleLabel,
                onValueChange = {},
                readOnly = true,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
            )
            InventraLabeledField(
                label = "Cozinha",
                value = cozinhaName,
                onValueChange = {},
                readOnly = true,
                placeholder = "",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun ProfileDetailsRoute(
    navigator: Navigator,
    viewModel: ProfileDetailsViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileDetailsScreen(
        fullName = uiState.fullName,
        login = uiState.login,
        roleLabel = uiState.roleLabel,
        cozinhaName = uiState.cozinhaName,
        onBackClick = { navigator.back() },
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ProfileDetailsScreenPreview() {
    ProfileDetailsScreen(
        fullName = "Jones Arakaki Kogake Passos Maldo",
        login = "jones.silva",
        roleLabel = "Gerente",
        cozinhaName = "Sabor Filial Sul",
        onBackClick = {},
    )
}
