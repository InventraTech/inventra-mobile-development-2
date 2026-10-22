package com.inventraoficial.inventra.feature.auth.register.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.inventraoficial.inventra.feature.auth.google.ui.GoogleSignInButton
import com.inventraoficial.inventra.feature.auth.google.ui.GoogleSignInHost
import com.inventraoficial.inventra.feature.auth.google.ui.GoogleSignInViewModel
import com.inventraoficial.inventra.feature.auth.google.ui.rememberGoogleSignInClick
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger

@Composable
fun RegisterScreen(
    role: InventraUserRole?,
    onRoleSelect: (InventraUserRole) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    login: String,
    onLoginChange: (String) -> Unit,
    senha: String,
    onPasswordChange: (String) -> Unit,
    confirmarSenha: String,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    isGoogleLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(InventraBackground),
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
        Column(
            modifier =
                modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp, 40.dp, 24.dp, 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                label = "Nome",
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Insira seu nome",
            )
            InventraLabeledField(
                label = "E-mail",
                value = login,
                onValueChange = onLoginChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Insira seu e-mail",
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
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onRegisterClick()
                },
                isLoading = isLoading,
                modifier =
                    Modifier
                        .width(300.dp)
                        .padding(0.dp, 0.dp, 0.dp, 0.dp),
            )
            GoogleSignInButton(
                onClick = onGoogleClick,
                isLoading = isGoogleLoading,
                modifier = Modifier.width(300.dp),
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
    viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory),
    googleViewModel: GoogleSignInViewModel = viewModel(factory = GoogleSignInViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val googleState by googleViewModel.uiState.collectAsState()
    GoogleSignInHost(uiState = googleState, viewModel = googleViewModel, navigator = navigator)

    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) {
            when (uiState.role) {
                InventraUserRole.Supervisor -> navigator.replaceStack(Screen.CreateCozinha)
                InventraUserRole.Estoquista,
                InventraUserRole.Comprador,
                -> navigator.replaceStack(Screen.SelectCozinha)
                null -> navigator.navigateTopLevel(Screen.Home)
            }
        }
    }

    RegisterScreen(
        role = uiState.role,
        onRoleSelect = viewModel::onRoleSelect,
        name = uiState.name,
        onNameChange = viewModel::onNameChange,
        login = uiState.login,
        onLoginChange = viewModel::onLoginChange,
        senha = uiState.password,
        onPasswordChange = viewModel::onPasswordChange,
        confirmarSenha = uiState.confirmPassword,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onRegisterClick = viewModel::onRegisterClick,
        onBackToLoginClick = { navigator.back() },
        onGoogleClick = rememberGoogleSignInClick(googleViewModel),
        errorMessage = uiState.errorMessage ?: googleState.screenErrorMessage,
        isLoading = uiState.isLoading,
        isGoogleLoading = googleState.isButtonLoading,
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
        name = "",
        onNameChange = {},
        login = "",
        onLoginChange = {},
        senha = "",
        onPasswordChange = {},
        confirmarSenha = "",
        onConfirmPasswordChange = {},
        onRegisterClick = {},
        onBackToLoginClick = {},
        onGoogleClick = {},
        errorMessage = null,
        isLoading = false,
        isGoogleLoading = false,
    )
}
