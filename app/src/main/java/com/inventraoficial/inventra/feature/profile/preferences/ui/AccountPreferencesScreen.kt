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
import androidx.compose.material3.HorizontalDivider
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
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

private const val THEME_TOGGLE_CORNER_PERCENT = 50

@Composable
fun AccountPreferencesScreen(
    notifyExpiring: Boolean,
    onNotifyExpiringChange: (Boolean) -> Unit,
    notifyLowStock: Boolean,
    onNotifyLowStockChange: (Boolean) -> Unit,
    notifyPendingApproval: Boolean,
    onNotifyPendingApprovalChange: (Boolean) -> Unit,
    theme: AppTheme,
    onThemeSelect: (AppTheme) -> Unit,
    language: String,
    onLanguageClick: () -> Unit,
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
                    "Preferências da conta",
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle(text = "Notificações")
                InventraSwitchRow(
                    label = "Produtos vencendo",
                    checked = notifyExpiring,
                    onCheckedChange = onNotifyExpiringChange,
                )
                InventraSwitchRow(
                    label = "Estoque abaixo do mínimo",
                    checked = notifyLowStock,
                    onCheckedChange = onNotifyLowStockChange,
                )
                InventraSwitchRow(
                    label = "Requisição pendente de aprovação",
                    checked = notifyPendingApproval,
                    onCheckedChange = onNotifyPendingApprovalChange,
                )
            }
            HorizontalDivider()
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(text = "Tema")
                ThemeSelector(selected = theme, onSelect = onThemeSelect)
            }
            HorizontalDivider()
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLanguageClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Idioma",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = InventraPurple,
                    modifier = Modifier.weight(1f),
                )
                Text(text = language, fontFamily = Montserrat, fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = "›",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    color = InventraPurple,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
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
private fun ThemeSelector(
    selected: AppTheme,
    onSelect: (AppTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(percent = THEME_TOGGLE_CORNER_PERCENT)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), shape),
    ) {
        AppTheme.entries.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .background(if (isSelected) InventraPurple else Color.White, shape)
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
    viewModel: AccountPreferencesViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    AccountPreferencesScreen(
        notifyExpiring = uiState.notifyExpiring,
        onNotifyExpiringChange = viewModel::onNotifyExpiringChange,
        notifyLowStock = uiState.notifyLowStock,
        onNotifyLowStockChange = viewModel::onNotifyLowStockChange,
        notifyPendingApproval = uiState.notifyPendingApproval,
        onNotifyPendingApprovalChange = viewModel::onNotifyPendingApprovalChange,
        theme = uiState.theme,
        onThemeSelect = viewModel::onThemeSelect,
        language = uiState.language,
        onLanguageClick = viewModel::onLanguageClick,
        onCloseClick = { navigator.back() },
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun AccountPreferencesScreenPreview() {
    AccountPreferencesScreen(
        notifyExpiring = true,
        onNotifyExpiringChange = {},
        notifyLowStock = false,
        onNotifyLowStockChange = {},
        notifyPendingApproval = false,
        onNotifyPendingApprovalChange = {},
        theme = AppTheme.Light,
        onThemeSelect = {},
        language = "Português (Brasil)",
        onLanguageClick = {},
        onCloseClick = {},
    )
}
