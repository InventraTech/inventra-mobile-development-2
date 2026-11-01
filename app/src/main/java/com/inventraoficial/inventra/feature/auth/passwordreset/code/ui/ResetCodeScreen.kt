package com.inventraoficial.inventra.feature.auth.passwordreset.code.ui

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
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple

@Composable
fun ResetCodeScreen(
    email: String,
    code: String,
    onCodeChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onResendClick: () -> Unit,
    onChangeEmailClick: () -> Unit,
    resendInSeconds: Int,
    canResend: Boolean,
    errorMessage: String?,
    infoMessage: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    PasswordResetLayout(
        subtitle = "Digite o código",
        description = "Se houver uma conta com $email, enviamos um código de 6 números. Ele vale por 15 minutos.",
        modifier = modifier,
    ) {
        InventraLabeledField(
            label = "Código",
            value = code,
            onValueChange = onCodeChange,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "000000",
        )
        PasswordResetMessage(errorMessage = errorMessage, infoMessage = infoMessage)
        InventraPrimaryButton(
            text = "Confirmar código",
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                onConfirmClick()
            },
            isLoading = isLoading,
            modifier = Modifier.width(300.dp),
        )
        InventraTextLink(
            text = if (resendInSeconds > 0) "Reenviar código em ${resendInSeconds}s" else "Reenviar código",
            onClick = onResendClick,
            color = if (canResend) InventraPurple else InventraMuted,
        )
        InventraTextLink(
            text = "Usar outro e-mail",
            onClick = onChangeEmailClick,
        )
    }
}

@Composable
fun ResetCodeRoute(
    email: String,
    navigator: Navigator,
    viewModel: ResetCodeViewModel = viewModel(factory = ResetCodeViewModel.factory(email)),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.resetToken) {
        uiState.resetToken?.let { token ->
            viewModel.onNewPasswordOpened()
            navigator.navigate(Screen.NewPassword(email, token))
        }
    }

    ResetCodeScreen(
        email = email,
        code = uiState.code,
        onCodeChange = viewModel::onCodeChange,
        onConfirmClick = viewModel::onConfirmClick,
        onResendClick = viewModel::onResendClick,
        onChangeEmailClick = { navigator.back() },
        resendInSeconds = uiState.resendInSeconds,
        canResend = uiState.canResend,
        errorMessage = uiState.errorMessage,
        infoMessage = uiState.infoMessage,
        isLoading = uiState.isLoading,
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ResetCodeScreenPreview() {
    ResetCodeScreen(
        email = "maria@inventra.com",
        code = "4829",
        onCodeChange = {},
        onConfirmClick = {},
        onResendClick = {},
        onChangeEmailClick = {},
        resendInSeconds = 42,
        canResend = false,
        errorMessage = null,
        infoMessage = null,
        isLoading = false,
    )
}
