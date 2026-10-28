package com.inventraoficial.inventra.feature.profile.team.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import com.inventraoficial.inventra.core.designsystem.atoms.InventraCloseButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraMemberRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun TeamScreen(
    uiState: TeamUiState,
    onRetryClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            navigationIcon = {
                InventraCloseButton(onClick = onCloseClick)
            },
            title = {
                Text(
                    "Equipe",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
            },
        )
        when {
            uiState.isLoading -> CenteredLoading()
            uiState.errorMessage != null -> ErrorState(message = uiState.errorMessage, onRetryClick = onRetryClick)
            else -> TeamContent(members = uiState.members)
        }
    }
}

@Composable
private fun TeamContent(members: List<TeamMember>) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Membros · ${members.size}",
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = InventraPurple,
        )
        members.forEach { member ->
            InventraMemberRow(
                initials = member.initials,
                name = member.name,
                roleLabel = member.roleLabel,
                isOwner = member.isCurrentUser,
                onDeactivateClick = null,
                modifier = Modifier.fillMaxWidth(),
                photoUrl = member.photoUrl,
            )
        }
    }
}

@Composable
private fun CenteredLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = InventraPurple)
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, fontFamily = Montserrat, fontSize = 15.sp, textAlign = TextAlign.Center)
        InventraSecondaryButton(text = "Tentar novamente", onClick = onRetryClick)
    }
}

@Composable
fun TeamRoute(
    navigator: Navigator,
    viewModel: TeamViewModel = viewModel(factory = TeamViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    TeamScreen(
        uiState = uiState,
        onRetryClick = viewModel::onRetryClick,
        onCloseClick = { navigator.back() },
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun TeamScreenPreview() {
    TeamScreen(
        uiState =
            TeamUiState(
                members =
                    listOf(
                        TeamMember(
                            id = "2",
                            initials = "JS",
                            name = "Jones Silva (você)",
                            roleLabel = "Supervisor",
                            isCurrentUser = true,
                        ),
                        TeamMember(
                            id = "3",
                            initials = "PL",
                            name = "Paula Lima",
                            roleLabel = "Estoquista",
                            isCurrentUser = false,
                        ),
                    ),
            ),
        onRetryClick = {},
        onCloseClick = {},
    )
}
