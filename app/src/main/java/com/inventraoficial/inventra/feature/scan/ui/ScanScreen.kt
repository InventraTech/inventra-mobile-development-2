package com.inventraoficial.inventra.feature.scan.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraProductImage
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScanOverlay
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun ScanScreen(
    uiState: ScanUiState,
    actions: ScanActions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            actions.onPermissionResult(granted)
        }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        val alreadyGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            actions.onPermissionResult(true)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.result) {
        when (uiState.result) {
            is ScanResult.Found -> haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            is ScanResult.NotRegistered -> haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            null -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.hasCameraPermission) {
            BarcodeCamera(
                isFlashOn = uiState.isFlashOn,
                onBarcodeDetected = actions.onBarcodeDetected,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(modifier = Modifier.fillMaxSize()) {
            InventraTopBar(
                navigationIcon = {
                    InventraBackButton(onClick = actions.onBackClick)
                },
                title = {
                    Text(
                        "Escanear produto",
                        color = Color.White,
                        fontFamily = Montserrat,
                        fontSize = 17.sp,
                    )
                },
                actions = {
                    IconButton(onClick = actions.onFlashClick) {
                        Icon(
                            imageVector = if (uiState.isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = if (uiState.isFlashOn) "Desligar lanterna" else "Ligar lanterna",
                            tint = Color.White,
                        )
                    }
                },
                modifier = Modifier.height(100.dp),
            )
            val frameColor =
                when {
                    uiState.errorMessage != null -> InventraDanger
                    uiState.result is ScanResult.NotRegistered -> InventraGold
                    uiState.result is ScanResult.Found -> InventraSuccess
                    else -> InventraPurple
                }
            InventraScanOverlay(modifier = Modifier.weight(1f), frameColor = frameColor) {
                ScanPanel(uiState = uiState, actions = actions)
            }
        }
    }

    if (uiState.isManualEntryOpen) {
        ManualEntryDialog(
            code = uiState.manualCode,
            onCodeChange = actions.onManualCodeChange,
            onSubmit = actions.onManualCodeSubmit,
            onDismiss = actions.onManualEntryDismiss,
        )
    }
}

@Composable
private fun ColumnScope.ScanPanel(
    uiState: ScanUiState,
    actions: ScanActions,
) {
    val result = uiState.result
    when {
        uiState.lookingUpBarcode != null -> {
            CircularProgressIndicator(color = InventraPurple)
            PanelText("Buscando o código ${uiState.lookingUpBarcode}...")
        }
        uiState.errorMessage != null -> {
            PanelText(uiState.errorMessage)
            InventraSecondaryButton(text = "Tentar novamente", onClick = actions.onScanAgainClick)
        }
        result is ScanResult.Found -> FoundPanel(result, uiState.canManageStock, actions)
        result is ScanResult.NotRegistered -> NotRegisteredPanel(result, uiState, actions)
        !uiState.hasCameraPermission -> {
            PanelText(uiState.permissionMessage ?: "Aguardando a permissão da câmera")
            InventraTextLink(text = "Digitar o código", onClick = actions.onManualEntryClick)
        }
        else -> {
            PanelText("Aponte a câmera para o código de barras")
            InventraTextLink(text = "Digitar o código", onClick = actions.onManualEntryClick)
        }
    }
}

@Composable
private fun FoundPanel(
    result: ScanResult.Found,
    canManageStock: Boolean,
    actions: ScanActions,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProductThumbnail(photoUrl = result.photoUrl)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                result.name,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = InventraPurple,
            )
            result.details?.let { Text(it, fontFamily = Montserrat, fontSize = 13.sp, color = InventraMuted) }
            Text(result.stock, fontFamily = Montserrat, fontSize = 14.sp)
            result.nextExpiration?.let {
                Text("Próxima validade: $it", fontFamily = Montserrat, fontSize = 13.sp, color = InventraMuted)
            }
        }
    }
    if (canManageStock) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InventraPrimaryButton(
                text = "Dar entrada",
                onClick = { actions.onStockEntryClick(result.productId) },
                modifier = Modifier.weight(1f),
            )
            InventraPrimaryButton(
                text = "Dar baixa",
                onClick = { actions.onStockOutClick(result.productId) },
                enabled = result.hasStock,
                modifier = Modifier.weight(1f),
            )
        }
    }
    InventraSecondaryButton(
        text = "Ver produto",
        onClick = { actions.onSeeProductClick(result.productId) },
        modifier = Modifier.fillMaxWidth(),
    )
    InventraTextLink(text = "Escanear outro", onClick = actions.onScanAgainClick)
}

@Composable
private fun NotRegisteredPanel(
    result: ScanResult.NotRegistered,
    uiState: ScanUiState,
    actions: ScanActions,
) {
    Box(
        modifier =
            Modifier
                .size(64.dp)
                .background(InventraGold.copy(alpha = 0.16f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Inventory2,
            contentDescription = null,
            tint = InventraGold,
            modifier = Modifier.size(32.dp),
        )
    }
    Text(
        "Produto novo por aqui",
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = InventraPurple,
    )
    Text(
        result.barcode,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 2.sp,
        modifier =
            Modifier
                .border(1.dp, InventraMuted.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
    )
    PanelText("Esse código ainda não foi cadastrado no catálogo desta cozinha.")
    result.suggestion?.let {
        Text("Parece ser: $it", fontFamily = Montserrat, fontSize = 14.sp, color = InventraMuted)
    }
    when {
        uiState.canCreateProduct ->
            InventraPrimaryButton(
                text = "Cadastrar produto",
                onClick = { actions.onCreateProductClick(result.barcode) },
                modifier = Modifier.fillMaxWidth(),
            )
        !uiState.canReport -> Unit
        result.isReported ->
            Text(
                "Avisamos o supervisor da cozinha.",
                fontFamily = Montserrat,
                fontSize = 14.sp,
                color = InventraSuccess,
            )
        else ->
            InventraPrimaryButton(
                text = "Avisar o supervisor",
                onClick = actions.onReportClick,
                isLoading = result.isReporting,
                modifier = Modifier.fillMaxWidth(),
            )
    }
    result.reportError?.let { Text(it, fontFamily = Montserrat, fontSize = 14.sp, color = InventraDanger) }
    InventraTextLink(text = "Escanear outro", onClick = actions.onScanAgainClick)
}

@Composable
private fun ProductThumbnail(photoUrl: String?) {
    InventraProductImage(
        photoUrl = photoUrl,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(56.dp),
    )
}

@Composable
private fun PanelText(text: String) {
    Text(text, fontFamily = Montserrat, fontSize = 15.sp, textAlign = TextAlign.Center)
}

@Composable
private fun ManualEntryDialog(
    code: String,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Digitar o código", fontFamily = Montserrat, fontWeight = FontWeight.Bold) },
        text = {
            InventraLabeledField(
                label = "Código de barras",
                value = code,
                onValueChange = onCodeChange,
                keyboardType = KeyboardType.Number,
                placeholder = "Ex.: 7891234567890",
            )
        },
        confirmButton = {
            TextButton(onClick = onSubmit, enabled = code.isNotBlank()) {
                Text("Buscar", fontFamily = Montserrat, color = InventraPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontFamily = Montserrat, color = InventraMuted)
            }
        },
    )
}

@Composable
fun ScanRoute(
    navigator: Navigator,
    viewModel: ScanViewModel = viewModel(factory = ScanViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    ScanScreen(
        uiState = uiState,
        actions =
            ScanActions(
                onBackClick = { navigator.back() },
                onFlashClick = viewModel::onFlashClick,
                onPermissionResult = viewModel::onPermissionResult,
                onBarcodeDetected = viewModel::onBarcodeDetected,
                onScanAgainClick = viewModel::onScanAgainClick,
                onStockOutClick = { productId -> navigator.navigate(Screen.StockOut(productId)) },
                onStockEntryClick = { productId -> navigator.navigate(Screen.StockEntry(productId)) },
                onSeeProductClick = { productId -> navigator.navigate(Screen.StockDetail(productId)) },
                onReportClick = viewModel::onReportClick,
                onCreateProductClick = { barcode ->
                    // Limpa o resultado: ao voltar do cadastro, a câmera já está pronta para o próximo código.
                    viewModel.onScanAgainClick()
                    navigator.navigate(Screen.ProductCreate(barcode))
                },
                onManualEntryClick = viewModel::onManualEntryClick,
                onManualCodeChange = viewModel::onManualCodeChange,
                onManualCodeSubmit = viewModel::onManualCodeSubmit,
                onManualEntryDismiss = viewModel::onManualEntryDismiss,
            ),
        modifier = modifier,
    )
}

private val previewActions =
    ScanActions(
        onBackClick = {},
        onFlashClick = {},
        onPermissionResult = {},
        onBarcodeDetected = {},
        onScanAgainClick = {},
        onStockOutClick = {},
        onStockEntryClick = {},
        onSeeProductClick = {},
        onReportClick = {},
        onCreateProductClick = {},
        onManualEntryClick = {},
        onManualCodeChange = {},
        onManualCodeSubmit = {},
        onManualEntryDismiss = {},
    )

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ScanFoundPreview() {
    ScanScreen(
        uiState =
            ScanUiState(
                canManageStock = true,
                result =
                    ScanResult.Found(
                        barcode = "7891234567890",
                        productId = 1,
                        name = "Leite Integral",
                        details = "Italac · Laticínios",
                        photoUrl = null,
                        stock = "12,5 L em 3 lotes",
                        nextExpiration = "20/10/2026",
                        hasStock = true,
                    ),
            ),
        actions = previewActions,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ScanNotRegisteredPreview() {
    ScanScreen(
        uiState =
            ScanUiState(
                canReport = true,
                result = ScanResult.NotRegistered(barcode = "7894900011517", suggestion = "Coca-Cola · 2 L"),
            ),
        actions = previewActions,
    )
}
