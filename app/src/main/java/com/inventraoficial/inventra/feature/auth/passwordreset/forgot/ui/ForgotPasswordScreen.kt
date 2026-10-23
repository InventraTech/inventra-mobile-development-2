package com.inventraoficial.inventra.feature.auth.passwordreset.forgot.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.feature.auth.passwordreset.PasswordResetLayout
import com.inventraoficial.inventra.feature.auth.passwordreset.PasswordResetMessage
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen

@Composable
fun ForgotPasswordScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    PasswordResetLayout(
        subtitle = "Esqueci minha senha",
        description = "Informe o e-mail da sua conta. Vamos enviar um código para você criar uma nova senha.",
        modifier = modifier,
    ) {
        InventraLabeledField(
            label = "E-mail",
            value = email,
            onValueChange = onEmailChange,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Insira seu e-mail",
        )
        PasswordResetMessage(errorMessage = errorMessage)
        InventraPrimaryButton(
            text = "Enviar código",
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                onSendClick()
            },
            isLoading = isLoading,
            modifier = Modifier.width(300.dp),
        )
        InventraTextLink(
            text = "Voltar para o login",
            onClick = onBackToLoginClick,
        )
    }
}

@Composable
fun ForgotPasswordRoute(
    email: String,
    navigator: Navigator,
    viewModel: ForgotPasswordViewModel = viewModel(factory = ForgotPasswordViewModel.factory(email)),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.codeSentTo) {
        uiState.codeSentTo?.let { sentTo ->
            viewModel.onCodeScreenOpened()
            navigator.navigate(Screen.ResetCode(sentTo))
        }
    }

    ForgotPasswordScreen(
        email = uiState.email,
        onEmailChange = viewModel::onEmailChange,
        onSendClick = viewModel::onSendClick,
        onBackToLoginClick = { navigator.back() },
        errorMessage = uiState.errorMessage,
        isLoading = uiState.isLoading,
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(
        email = "maria@inventra.com",
        onEmailChange = {},
        onSendClick = {},
        onBackToLoginClick = {},
        errorMessage = null,
        isLoading = false,
    )
}
