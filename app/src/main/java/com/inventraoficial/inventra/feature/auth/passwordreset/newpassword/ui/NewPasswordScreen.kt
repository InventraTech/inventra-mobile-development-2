package com.inventraoficial.inventra.feature.auth.passwordreset.newpassword.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.feature.auth.passwordreset.PasswordResetLayout
import com.inventraoficial.inventra.feature.auth.passwordreset.PasswordResetMessage
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen

@Composable
fun NewPasswordScreen(
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onGoToLoginClick: () -> Unit,
    onRequestNewCodeClick: () -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    isDone: Boolean,
    isTokenExpired: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    when {
        isDone ->
            PasswordResetLayout(
                subtitle = "Senha alterada",
                description = "Pronto! Agora é só entrar com a nova senha.",
                modifier = modifier,
            ) {
                InventraPrimaryButton(
                    text = "Ir para o login",
                    onClick = onGoToLoginClick,
                    modifier = Modifier.width(300.dp),
                )
            }
        isTokenExpired ->
            PasswordResetLayout(
                subtitle = "Código expirado",
                description = errorMessage.orEmpty(),
                modifier = modifier,
            ) {
                InventraPrimaryButton(
                    text = "Pedir novo código",
                    onClick = onRequestNewCodeClick,
                    modifier = Modifier.width(300.dp),
                )
            }
        else ->
            PasswordResetLayout(
                subtitle = "Crie uma nova senha",
                description = "A nova senha precisa ter pelo menos 8 caracteres.",
                modifier = modifier,
            ) {
                InventraLabeledField(
                    label = "Nova senha",
                    value = password,
                    onValueChange = onPasswordChange,
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Insira a nova senha",
                )
                InventraLabeledField(
                    label = "Confirmar senha",
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Confirme a nova senha",
                )
                PasswordResetMessage(errorMessage = errorMessage)
                InventraPrimaryButton(
                    text = "Salvar nova senha",
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onSaveClick()
                    },
                    isLoading = isLoading,
                    modifier = Modifier.width(300.dp),
                )
            }
    }
}

@Composable
fun NewPasswordRoute(
    email: String,
    resetToken: String,
    navigator: Navigator,
    viewModel: NewPasswordViewModel = viewModel(factory = NewPasswordViewModel.factory(resetToken)),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    NewPasswordScreen(
        password = uiState.password,
        onPasswordChange = viewModel::onPasswordChange,
        confirmPassword = uiState.confirmPassword,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSaveClick = viewModel::onSaveClick,
        onGoToLoginClick = { navigator.replaceStack(Screen.Login) },
        onRequestNewCodeClick = {
            navigator.replaceStack(Screen.Login)
            navigator.navigate(Screen.ForgotPassword(email))
        },
        errorMessage = uiState.errorMessage,
        isLoading = uiState.isLoading,
        isDone = uiState.isDone,
        isTokenExpired = uiState.isTokenExpired,
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun NewPasswordScreenPreview() {
    NewPasswordScreen(
        password = "",
        onPasswordChange = {},
        confirmPassword = "",
        onConfirmPasswordChange = {},
        onSaveClick = {},
        onGoToLoginClick = {},
        onRequestNewCodeClick = {},
        errorMessage = null,
        isLoading = false,
        isDone = false,
        isTokenExpired = false,
    )
}
