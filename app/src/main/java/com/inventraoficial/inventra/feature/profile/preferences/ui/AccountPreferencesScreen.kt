package com.inventraoficial.inventra.feature.profile.preferences.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraCloseButton
import com.inventraoficial.inventra.core.designsystem.molecules.InventraSwitchRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScreenContent
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.data.local.ThemeMode
import com.inventraoficial.inventra.feature.notifications.rememberNotificationPermissionRequest
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraBrand
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSurface
import com.inventraoficial.inventra.ui.theme.Montserrat

private const val THEME_TOGGLE_CORNER_PERCENT = 50

@Composable
fun AccountPreferencesScreen(
    theme: ThemeMode,
    onThemeSelect: (ThemeMode) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    alertNotifications: Boolean = true,
    onAlertNotificationsChange: (Boolean) -> Unit = {},
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
                    "Preferências da conta",
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionTitle(text = "Tema")
                ThemeSelector(selected = theme, onSelect = onThemeSelect)
                Text(
                    text = "Em \"Sistema\", o app acompanha o modo claro ou escuro do celular.",
                    fontFamily = Montserrat,
                    fontSize = 13.sp,
                    color = InventraMuted,
                )
                SectionTitle(text = "Notificações", modifier = Modifier.padding(top = 12.dp))
                InventraSwitchRow(
                    label = "Avisos no celular",
                    checked = alertNotifications,
                    onCheckedChange = onAlertNotificationsChange,
                )
                Text(
                    text =
                        "Estoque crítico, validade, requisições e pedidos da cozinha chegam mesmo com o app " +
                            "fechado. O celular confere a cada 15 minutos, mais ou menos.",
                    fontFamily = Montserrat,
                    fontSize = 13.sp,
                    color = InventraMuted,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = InventraPurple,
    )
}

@Composable
private fun ThemeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(percent = THEME_TOGGLE_CORNER_PERCENT)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), shape),
    ) {
        ThemeMode.entries.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .background(if (isSelected) InventraBrand else InventraSurface, shape)
                        .clickable(onClick = { onSelect(option) })
                        .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option.label,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else InventraPurple,
                )
            }
        }
    }
}

@Composable
fun AccountPreferencesRoute(
    navigator: Navigator,
    viewModel: AccountPreferencesViewModel = viewModel(factory = AccountPreferencesViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val requestNotificationPermission = rememberNotificationPermissionRequest()

    AccountPreferencesScreen(
        theme = uiState.theme,
        onThemeSelect = viewModel::onThemeSelect,
        onCloseClick = { navigator.back() },
        modifier = modifier,
        alertNotifications = uiState.alertNotifications,
        onAlertNotificationsChange = { enabled ->
            viewModel.onAlertNotificationsChange(enabled)
            if (enabled) requestNotificationPermission()
        },
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun AccountPreferencesScreenPreview() {
    AccountPreferencesScreen(
        theme = ThemeMode.System,
        onThemeSelect = {},
        onCloseClick = {},
    )
}
