package com.inventraoficial.inventra.feature.profile.members.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraCloseButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraMemberRow
import com.inventraoficial.inventra.core.designsystem.molecules.InventraPendingMemberRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun ManageMembersScreen(
    pendingRequests: List<PendingMemberRequest>,
    members: List<CozinhaMember>,
    onAcceptClick: (PendingMemberRequest) -> Unit,
    onRejectClick: (PendingMemberRequest) -> Unit,
    onRemoveClick: (CozinhaMember) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        InventraTopBar(
            navigationIcon = {
                InventraCloseButton(onClick = onCloseClick)
            },
            title = {
                Text(
                    "Gerenciar Membros",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
            },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (pendingRequests.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(text = "Solicitações pendentes")
                    pendingRequests.forEach { request ->
                        InventraPendingMemberRow(
                            initials = request.initials,
                            name = request.name,
                            roleLabel = request.roleLabel,
                            onAcceptClick = { onAcceptClick(request) },
                            onRejectClick = { onRejectClick(request) },
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(text = "Membros · ${members.size}")
                members.forEach { member ->
                    InventraMemberRow(
                        initials = member.initials,
                        name = member.name,
                        roleLabel = member.roleLabel,
                        isOwner = member.isOwner,
                        onRemoveClick = { onRemoveClick(member) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = InventraPurple,
    )
}

@Composable
fun ManageMembersRoute(
    navigator: Navigator,
    viewModel: ManageMembersViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    ManageMembersScreen(
        pendingRequests = uiState.pendingRequests,
        members = uiState.members,
        onAcceptClick = viewModel::onAcceptClick,
        onRejectClick = viewModel::onRejectClick,
        onRemoveClick = viewModel::onRemoveClick,
        onCloseClick = { navigator.back() },
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ManageMembersScreenPreview() {
    ManageMembersScreen(
        pendingRequests =
            listOf(
                PendingMemberRequest(initials = "MR", name = "Mauro Rosa", roleLabel = "Estoquista"),
            ),
        members =
            listOf(
                CozinhaMember(initials = "JS", name = "Jones Silva (você)", roleLabel = "Gerente", isOwner = true),
            ),
        onAcceptClick = {},
        onRejectClick = {},
        onRemoveClick = {},
        onCloseClick = {},
    )
}
