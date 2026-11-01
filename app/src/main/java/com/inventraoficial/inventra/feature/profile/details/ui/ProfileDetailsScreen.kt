package com.inventraoficial.inventra.feature.profile.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraDangerButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraInitialsAvatar
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.core.designsystem.molecules.InventraConfirmDialog
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScreenContent
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.feature.profile.photo.ui.ProfilePhotoHost
import com.inventraoficial.inventra.feature.profile.photo.ui.ProfilePhotoViewModel
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.Montserrat

private val AVATAR_SIZE = 96.dp

@Composable
fun ProfileDetailsScreen(
    fullName: String,
    email: String,
    roleLabel: String,
    cozinhaName: String,
    initials: String,
    photoUrl: String?,
    isPhotoUploading: Boolean,
    photoErrorMessage: String?,
    onChangePhotoClick: () -> Unit,
    onBackClick: () -> Unit,
    isLogoutDialogVisible: Boolean,
    onLogoutClick: () -> Unit,
    onLogoutConfirm: () -> Unit,
    onLogoutDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    leaveKitchen: LeaveKitchenState = LeaveKitchenState(),
    onLeaveKitchenClick: () -> Unit = {},
    onLeaveKitchenConfirm: () -> Unit = {},
    onLeaveKitchenDismiss: () -> Unit = {},
) {
    if (leaveKitchen.isDialogVisible) {
        InventraConfirmDialog(
            title = "Sair da cozinha?",
            message =
                "Você deixa de ver o estoque e as requisições de $cozinhaName, e seus rascunhos de " +
                    "requisição são cancelados. Sua conta continua ativa para entrar em outra cozinha.",
            confirmText = "Sair da cozinha",
            onConfirm = onLeaveKitchenConfirm,
            onDismiss = onLeaveKitchenDismiss,
            isDestructive = true,
        )
    }

    if (isLogoutDialogVisible) {
        InventraConfirmDialog(
            title = "Sair da conta?",
            message = "Você precisará entrar com seu e-mail e senha novamente.",
            confirmText = "Sair",
            onConfirm = onLogoutConfirm,
            onDismiss = onLogoutDismiss,
            isDestructive = true,
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
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
        InventraScreenContent {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .clip(CircleShape)
                                .clickable(enabled = !isPhotoUploading, onClick = onChangePhotoClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        InventraInitialsAvatar(initials = initials, photoUrl = photoUrl, size = AVATAR_SIZE)
                        if (isPhotoUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                    InventraTextLink(
                        text = if (photoUrl == null) "Adicionar foto" else "Trocar foto",
                        onClick = onChangePhotoClick,
                    )
                    photoErrorMessage?.let { message ->
                        Text(
                            text = message,
                            fontFamily = Montserrat,
                            fontSize = 13.sp,
                            color = InventraDanger,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                InventraLabeledField(
                    label = "Nome completo",
                    value = fullName,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = "",
                    modifier = Modifier.fillMaxWidth(),
                )
                InventraLabeledField(
                    label = "E-mail",
                    value = email,
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
                if (leaveKitchen.canLeave) {
                    LeaveKitchenLink(state = leaveKitchen, onClick = onLeaveKitchenClick)
                }
            }
            InventraDangerButton(
                text = "Sair da conta",
                onClick = onLogoutClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            )
        }
    }
}

@Composable
private fun LeaveKitchenLink(
    state: LeaveKitchenState,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (state.isLeaving) {
            CircularProgressIndicator(color = InventraDanger, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            InventraTextLink(text = "Sair da cozinha", onClick = onClick, color = InventraDanger)
        }
        state.errorMessage?.let { message ->
            Text(text = message, fontFamily = Montserrat, fontSize = 13.sp, color = InventraDanger)
        }
    }
}

@Composable
fun ProfileDetailsRoute(
    navigator: Navigator,
    viewModel: ProfileDetailsViewModel = viewModel(factory = ProfileDetailsViewModel.Factory),
    photoViewModel: ProfilePhotoViewModel = viewModel(factory = ProfilePhotoViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val photoState by photoViewModel.uiState.collectAsState()
    ProfilePhotoHost(uiState = photoState, viewModel = photoViewModel)
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) navigator.replaceStack(Screen.Login)
    }
    LaunchedEffect(uiState.leaveKitchen.hasLeft) {
        if (uiState.leaveKitchen.hasLeft) {
            navigator.replaceStack(if (uiState.leaveKitchen.isSupervisor) Screen.CreateCozinha else Screen.RequestSent)
        }
    }

    ProfileDetailsScreen(
        fullName = uiState.fullName,
        email = uiState.email,
        roleLabel = uiState.roleLabel,
        cozinhaName = uiState.cozinhaName,
        initials = photoState.initials,
        photoUrl = photoState.photoUrl,
        isPhotoUploading = photoState.isUploading,
        photoErrorMessage = photoState.errorMessage,
        onChangePhotoClick = photoViewModel::onAvatarClick,
        onBackClick = { navigator.back() },
        isLogoutDialogVisible = uiState.isLogoutDialogVisible,
        onLogoutClick = viewModel::onLogoutClick,
        onLogoutConfirm = viewModel::onLogoutConfirm,
        onLogoutDismiss = viewModel::onLogoutDismiss,
        modifier = modifier,
        leaveKitchen = uiState.leaveKitchen,
        onLeaveKitchenClick = viewModel::onLeaveKitchenClick,
        onLeaveKitchenConfirm = viewModel::onLeaveKitchenConfirm,
        onLeaveKitchenDismiss = viewModel::onLeaveKitchenDismiss,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ProfileDetailsScreenPreview() {
    ProfileDetailsScreen(
        fullName = "Jones Arakaki Kogake Passos Maldo",
        email = "jones.silva@gmail.com",
        roleLabel = "Gerente",
        cozinhaName = "Sabor Filial Sul",
        initials = "JM",
        photoUrl = null,
        isPhotoUploading = false,
        photoErrorMessage = null,
        onChangePhotoClick = {},
        onBackClick = {},
        isLogoutDialogVisible = false,
        onLogoutClick = {},
        onLogoutConfirm = {},
        onLogoutDismiss = {},
        leaveKitchen = LeaveKitchenState(canLeave = true),
    )
}
