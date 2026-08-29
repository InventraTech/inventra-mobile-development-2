package com.inventraoficial.inventra.feature.auth.ui

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
import com.inventraoficial.inventra.core.designsystem.organisms.InventraAuthHeader

@Composable
fun LoginScreen(
    login: String,
    onLoginChange: (String) -> Unit,
    senha: String,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
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
            modifier =
                Modifier
                    .align(Alignment.BottomStart),
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
            InventraPrimaryButton(
                text = "Entrar",
                onClick = onLoginClick,
                modifier = Modifier.width(300.dp).padding(0.dp, 35.dp, 0.dp, 20.dp),
            )
            InventraTextLink(
                text = "Esqueci minha senha",
                onClick = onForgotPasswordClick,
            )
        }
    }
}

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LoginScreen(
        login = uiState.login,
        onLoginChange = viewModel::onLoginChange,
        senha = uiState.password,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onLoginClick,
        onForgotPasswordClick = viewModel::onForgotPasswordClick,
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
    )
}
