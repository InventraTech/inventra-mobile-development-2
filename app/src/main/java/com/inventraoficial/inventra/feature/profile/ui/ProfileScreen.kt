package com.inventraoficial.inventra.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.molecules.InventraMenuRow
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

private val HEADER_HEIGHT = 135.dp
private val AVATAR_SIZE = 90.dp

@Composable
fun ProfileScreen(
    fullName: String,
    roleLabel: String,
    cozinhaName: String,
    onProfileDetailsClick: () -> Unit,
    onManageMembersClick: () -> Unit,
    onAccountPreferencesClick: () -> Unit,
    onCozinhaInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
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
                        .background(InventraPurple, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
            )
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = (HEADER_HEIGHT - AVATAR_SIZE) / 2 - 8.dp)
                        .size(AVATAR_SIZE)
                        .border(3.dp, Color.White, CircleShape)
                        .background(Color.Black, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp),
                )
            }
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
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
        )
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
            InventraMenuRow(
                icon = Icons.Default.Groups,
                label = "Gerenciar membros",
                onClick = onManageMembersClick,
            )
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
fun ProfileRoute(
    navigator: Navigator,
    viewModel: ProfileViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileScreen(
        fullName = uiState.fullName,
        roleLabel = uiState.roleLabel,
        cozinhaName = uiState.cozinhaName,
        onProfileDetailsClick = { navigator.navigate(Screen.ProfileDetails) },
        onManageMembersClick = { navigator.navigate(Screen.ManageMembers) },
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
        onProfileDetailsClick = {},
        onManageMembersClick = {},
        onAccountPreferencesClick = {},
        onCozinhaInfoClick = {},
    )
}
