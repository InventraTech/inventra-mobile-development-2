package com.inventraoficial.inventra.feature.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraChatInputBar
import com.inventraoficial.inventra.core.designsystem.molecules.InventraSuggestionChip
import com.inventraoficial.inventra.core.designsystem.organisms.InventraChatEmptyHeader
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun ChatScreen(
    greeting: String,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    inputValue: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
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
                InventraBackButton(onClick = onBackClick)
            },
            title = {
                Text(
                    "Assistente",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontSize = 17.sp,
                )
            },
            actions = {
                Text(
                    "☰",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.clickable(onClick = onMenuClick),
                )
            },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            InventraChatEmptyHeader(greeting = greeting)
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                suggestions.forEach { suggestion ->
                    InventraSuggestionChip(
                        text = suggestion,
                        onClick = { onSuggestionClick(suggestion) },
                    )
                }
            }
        }
        InventraChatInputBar(
            value = inputValue,
            onValueChange = onInputChange,
            onSend = onSend,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
fun ChatRoute(
    navigator: Navigator,
    viewModel: ChatViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    ChatScreen(
        greeting = uiState.greeting,
        suggestions = uiState.suggestions,
        onSuggestionClick = viewModel::onSuggestionClick,
        inputValue = uiState.inputValue,
        onInputChange = viewModel::onInputChange,
        onSend = viewModel::onSend,
        onBackClick = { navigator.back() },
        onMenuClick = viewModel::onMenuClick,
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ChatScreenPreview() {
    ChatScreen(
        greeting = "Olá! 👋",
        suggestions =
            listOf(
                "Quais produtos vencem essa semana?",
                "Qual fornecedor tem a melhor nota?",
                "Quanto eu economizei esse mês?",
            ),
        onSuggestionClick = {},
        inputValue = "",
        onInputChange = {},
        onSend = {},
        onBackClick = {},
        onMenuClick = {},
    )
}
