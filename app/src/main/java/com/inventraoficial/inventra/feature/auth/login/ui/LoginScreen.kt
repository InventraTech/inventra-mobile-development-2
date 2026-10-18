package com.inventraoficial.inventra.feature.auth.login.ui

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
fun LoginScreen(
    login: String,
    onLoginChange: (String) -> Unit,
    senha: String,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
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
        Image(
            painter = painterResource(id = R.drawable.bg_shape_purple_bottom),
            contentDescription = "Background shape yellow bottom",
            modifier =
                Modifier
                    .align(Alignment.BottomStart),
        )
        Column(
            modifier =
                modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp, 64.dp, 24.dp, 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            InventraAuthHeader(
                appName = "Inventra",
                subtitle = "Seja bem-vindo(a) ao Inventra!",
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
            InventraTextLink(
                text = "Esqueci minha senha",
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.End),
            )
            Text(
                text = errorMessage.orEmpty(),
                color = InventraDanger,
                minLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            InventraPrimaryButton(
                text = "Entrar",
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onLoginClick()
                },
                isLoading = isLoading,
                modifier = Modifier.width(300.dp),
            )
            GoogleSignInButton(
                onClick = onGoogleClick,
                isLoading = isGoogleLoading,
                modifier = Modifier.width(300.dp),
            )
            InventraTextLink(
                text = "Criar conta",
                onClick = onCreateAccountClick,
            )
        }
    }
}

@Composable
fun LoginRoute(
    navigator: Navigator,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
    googleViewModel: GoogleSignInViewModel = viewModel(factory = GoogleSignInViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val googleState by googleViewModel.uiState.collectAsState()
    GoogleSignInHost(uiState = googleState, viewModel = googleViewModel, navigator = navigator)

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            when {
                uiState.mustCreateKitchen -> navigator.replaceStack(Screen.CreateCozinha)
                uiState.mustJoinKitchen -> navigator.replaceStack(Screen.RequestSent)
                else -> navigator.navigateTopLevel(Screen.Home)
            }
        }
    }

    LoginScreen(
        login = uiState.login,
        onLoginChange = viewModel::onLoginChange,
        senha = uiState.password,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onLoginClick,
        onForgotPasswordClick = viewModel::onForgotPasswordClick,
        onCreateAccountClick = { navigator.navigate(Screen.Register) },
        onGoogleClick = rememberGoogleSignInClick(googleViewModel),
        errorMessage = uiState.errorMessage ?: googleState.screenErrorMessage,
        isLoading = uiState.isLoading,
        isGoogleLoading = googleState.isButtonLoading,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        login = "",
        onLoginChange = {},
        senha = "",
        onPasswordChange = {},
        onLoginClick = {},
        onForgotPasswordClick = {},
        onCreateAccountClick = {},
        onGoogleClick = {},
        errorMessage = null,
        isLoading = false,
        isGoogleLoading = false,
    )
}
