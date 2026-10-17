package com.inventraoficial.inventra.feature.auth.google.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraRoleSelector
import com.inventraoficial.inventra.core.designsystem.molecules.InventraUserRole
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = InventraMuted.copy(alpha = 0.4f))
            Text(
                text = "ou",
                color = InventraMuted,
                fontFamily = Montserrat,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = InventraMuted.copy(alpha = 0.4f))
        }
        InventraSecondaryButton(
            text = if (isLoading) "Entrando..." else "Continuar com Google",
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleRoleSheet(
    signUp: GoogleSignUp,
    errorMessage: String?,
    isLoading: Boolean,
    onRoleSelect: (InventraUserRole) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = InventraBackground,
    ) {
        GoogleRoleSheetContent(
            signUp = signUp,
            errorMessage = errorMessage,
            isLoading = isLoading,
            onRoleSelect = onRoleSelect,
            onConfirm = onConfirm,
        )
    }
}

@Composable
fun GoogleRoleSheetContent(
    signUp: GoogleSignUp,
    errorMessage: String?,
    isLoading: Boolean,
    onRoleSelect: (InventraUserRole) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = signUp.firstName?.let { "Olá, $it!" } ?: "Olá!",
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = InventraInk,
        )
        Text(
            text = "Falta pouco para criar sua conta com ${signUp.email}. Qual é o seu cargo?",
            fontFamily = Montserrat,
            fontSize = 14.sp,
            color = InventraMuted,
        )
        InventraRoleSelector(
            selectedRole = signUp.role,
            onRoleSelect = onRoleSelect,
        )
        Text(
            text = errorMessage.orEmpty(),
            color = InventraDanger,
            minLines = 1,
        )
        InventraPrimaryButton(
            text = "Criar conta",
            onClick = onConfirm,
            isLoading = isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun GoogleSignInHost(
    uiState: GoogleSignInUiState,
    viewModel: GoogleSignInViewModel,
    navigator: Navigator,
) {
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            when {
                uiState.mustCreateKitchen -> navigator.replaceStack(Screen.CreateCozinha)
                uiState.mustJoinKitchen -> navigator.replaceStack(Screen.RequestSent)
                else -> navigator.navigateTopLevel(Screen.Home)
            }
        }
    }

    uiState.pendingSignUp?.let { signUp ->
        GoogleRoleSheet(
            signUp = signUp,
            errorMessage = uiState.errorMessage,
            isLoading = uiState.isLoading,
            onRoleSelect = viewModel::onRoleSelect,
            onConfirm = viewModel::onConfirmSignUp,
            onDismiss = viewModel::onDismissSignUp,
        )
    }
}

@Composable
fun rememberGoogleSignInClick(viewModel: GoogleSignInViewModel): () -> Unit {
    val context = LocalContext.current
    return remember(viewModel, context) {
        val googleAuthClient = (context.applicationContext as InventraApplication).container.googleAuthClient
        { viewModel.onGoogleClick { googleAuthClient.signIn(context) } }
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun GoogleRoleSheetContentPreview() {
    GoogleRoleSheetContent(
        signUp = GoogleSignUp(idToken = "", name = "Felipe Kogake", email = "felipe@gmail.com"),
        errorMessage = null,
        isLoading = false,
        onRoleSelect = {},
        onConfirm = {},
    )
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun GoogleSignInButtonPreview() {
    GoogleSignInButton(onClick = {}, isLoading = false)
}
