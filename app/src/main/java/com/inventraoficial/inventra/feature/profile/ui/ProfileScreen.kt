package com.inventraoficial.inventra.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.inventraoficial.inventra.core.designsystem.molecules.InventraMenuRow
import com.inventraoficial.inventra.feature.profile.photo.ui.ProfilePhotoHost
import com.inventraoficial.inventra.feature.profile.photo.ui.ProfilePhotoViewModel
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraBrand
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.Montserrat

private val HEADER_HEIGHT = 135.dp
private val AVATAR_SIZE = 90.dp

@Composable
fun ProfileScreen(
    fullName: String,
    roleLabel: String,
    cozinhaName: String,
    canSeeTeam: Boolean,
    photoUrl: String?,
    isPhotoUploading: Boolean,
    photoErrorMessage: String?,
    onAvatarClick: () -> Unit,
    onProfileDetailsClick: () -> Unit,
    onTeamClick: () -> Unit,
    onAccountPreferencesClick: () -> Unit,
    onCozinhaInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(HEADER_HEIGHT),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(HEADER_HEIGHT - AVATAR_SIZE / 2)
                        .background(InventraBrand, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
            )
            ProfileAvatar(
                photoUrl = photoUrl,
                isUploading = isPhotoUploading,
                onClick = onAvatarClick,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = (HEADER_HEIGHT - AVATAR_SIZE) / 2 - 8.dp),
            )
        }
        Text(
            text = fullName,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        )
        Text(
            text = "$roleLabel · $cozinhaName",
            fontFamily = Montserrat,
            fontSize = 13.sp,
            color = InventraMuted,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (photoErrorMessage == null) 24.dp else 8.dp),
        )
        photoErrorMessage?.let { message ->
            Text(
                text = message,
                fontFamily = Montserrat,
                fontSize = 13.sp,
                color = InventraDanger,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InventraMenuRow(
                icon = Icons.Default.Person,
                label = "Dados do perfil",
                onClick = onProfileDetailsClick,
            )
            if (canSeeTeam) {
                InventraMenuRow(
                    icon = Icons.Default.Groups,
                    label = "Ver equipe",
                    onClick = onTeamClick,
                )
            }
            InventraMenuRow(
                icon = Icons.Default.Tune,
                label = "Preferências da conta",
                onClick = onAccountPreferencesClick,
            )
            InventraMenuRow(
                icon = Icons.Default.Storefront,
                label = "Informações da cozinha",
                onClick = onCozinhaInfoClick,
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    photoUrl: String?,
    isUploading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(AVATAR_SIZE)
                .clip(CircleShape)
                .background(Color.Black)
                .border(3.dp, Color.White, CircleShape)
                .clickable(enabled = !isUploading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUrl == null) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp),
            )
        } else {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
        if (isUploading) {
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun ProfileRoute(
    navigator: Navigator,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
    photoViewModel: ProfilePhotoViewModel = viewModel(factory = ProfilePhotoViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val photoState by photoViewModel.uiState.collectAsState()
    ProfilePhotoHost(uiState = photoState, viewModel = photoViewModel)

    ProfileScreen(
        fullName = uiState.fullName,
        roleLabel = uiState.roleLabel,
        cozinhaName = uiState.cozinhaName,
        canSeeTeam = uiState.canSeeTeam,
        photoUrl = photoState.photoUrl,
        isPhotoUploading = photoState.isUploading,
        photoErrorMessage = photoState.errorMessage,
        onAvatarClick = photoViewModel::onAvatarClick,
        onProfileDetailsClick = { navigator.navigate(Screen.ProfileDetails) },
        onTeamClick = { navigator.navigate(Screen.Team) },
        onAccountPreferencesClick = { navigator.navigate(Screen.AccountPreferences) },
        onCozinhaInfoClick = { navigator.navigate(Screen.CozinhaInfo) },
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(
        fullName = "Jones Arakaki Kogake Passos Maldo",
        roleLabel = "Gerente",
        cozinhaName = "Sabor Filial Sul",
        canSeeTeam = true,
        photoUrl = null,
        isPhotoUploading = false,
        photoErrorMessage = null,
        onAvatarClick = {},
        onProfileDetailsClick = {},
        onTeamClick = {},
        onAccountPreferencesClick = {},
        onCozinhaInfoClick = {},
    )
}
