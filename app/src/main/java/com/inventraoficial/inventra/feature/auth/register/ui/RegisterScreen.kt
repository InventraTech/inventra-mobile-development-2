package com.inventraoficial.inventra.feature.auth.register.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.core.designsystem.molecules.InventraRoleSelector
import com.inventraoficial.inventra.core.designsystem.molecules.InventraUserRole
import com.inventraoficial.inventra.core.designsystem.organisms.InventraAuthHeader
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraDanger

@Composable
fun RegisterScreen(
    role: InventraUserRole?,
    onRoleSelect: (InventraUserRole) -> Unit,
    login: String,
    onLoginChange: (String) -> Unit,
    senha: String,
    onPasswordChange: (String) -> Unit,
    confirmarSenha: String,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_shape_yellow_top),
            contentDescription = "Background shape yellow top",
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .size(200.dp)
                    .offset(0.dp, -30.dp),
        )
        Image(
            painter = painterResource(id = R.drawable.bg_shape_purple_bottom),
            contentDescription = "Background shape yellow bottom",
            modifier = Modifier.align(Alignment.BottomStart),
        )
        Column(
            modifier =
                modifier
                    .padding(24.dp, 80.dp, 24.dp, 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            InventraAuthHeader(
                appName = "Inventra",
                subtitle = "Crie sua conta",
            )
            InventraRoleSelector(
                selectedRole = role,
                onRoleSelect = onRoleSelect,
            )
            InventraLabeledField(
                label = "Login",
                value = login,
                onValueChange = onLoginChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Insira seu Login",
            )
            InventraLabeledField(
                label = "Senha",
                value = senha,
                onValueChange = onPasswordChange,
                isPassword = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Insira sua Senha",
            )
            InventraLabeledField(
                label = "Confirmar senha",
                value = confirmarSenha,
                onValueChange = onConfirmPasswordChange,
                isPassword = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Confirme sua Senha",
            )
            Text(
                text = errorMessage.orEmpty(),
                color = InventraDanger,
                minLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            InventraPrimaryButton(
                text = "Criar",
                onClick = onRegisterClick,
                modifier =
                    Modifier
                        .width(300.dp)
                        .padding(0.dp, 15.dp, 0.dp, 20.dp),
            )
            InventraTextLink(
                text = "Já tenho uma conta!",
                onClick = onBackToLoginClick,
            )
        }
    }
}

@Composable
fun RegisterRoute(
    navigator: Navigator,
    viewModel: RegisterViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    RegisterScreen(
        role = uiState.role,
        onRoleSelect = viewModel::onRoleSelect,
        login = uiState.login,
        onLoginChange = viewModel::onLoginChange,
        senha = uiState.password,
        onPasswordChange = viewModel::onPasswordChange,
        confirmarSenha = uiState.confirmPassword,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onRegisterClick = {
            if (viewModel.onRegisterClick()) {
                navigator.navigateTopLevel(Screen.Home)
            }
        },
        onBackToLoginClick = { navigator.back() },
        errorMessage = uiState.errorMessage,
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun RegisterScreenPreview() {
    RegisterScreen(
        role = InventraUserRole.Supervisor,
        onRoleSelect = {},
        login = "",
        onLoginChange = {},
        senha = "",
        onPasswordChange = {},
        confirmarSenha = "",
        onConfirmPasswordChange = {},
        onRegisterClick = {},
        onBackToLoginClick = {},
        errorMessage = null,
    )
}
