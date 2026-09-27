package com.inventraoficial.inventra.feature.cozinha.requestsent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
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
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun RequestSentScreen(
    cozinhaName: String,
    onUnderstoodClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(96.dp)
                    .background(InventraGold.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = InventraGold,
                modifier = Modifier.size(48.dp),
            )
        }
        Text(
            text = "Solicitação enviada",
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            text =
                "Sua solicitação para entrar na cozinha $cozinhaName foi enviada. " +
                    "Você receberá uma notificação assim que o sistema aprovar.",
            fontFamily = Montserrat,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        InventraPrimaryButton(
            text = "Entendi!",
            onClick = onUnderstoodClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
        )
    }
}

@Composable
fun RequestSentRoute(
    navigator: Navigator,
    viewModel: RequestSentViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    RequestSentScreen(
        cozinhaName = uiState.cozinhaName,
        onUnderstoodClick = { navigator.back() },
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun RequestSentScreenPreview() {
    RequestSentScreen(
        cozinhaName = "Restaurante Sabor Caseiro",
        onUnderstoodClick = {},
    )
}
