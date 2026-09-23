package com.inventraoficial.inventra.feature.scan.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScanOverlay
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.Montserrat
import androidx.camera.core.Preview as CameraPreview

@Composable
fun ScanScreen(
    instruction: String,
    onBackClick: () -> Unit,
    onFlashClick: () -> Unit,
    onScan: () -> Unit,
    onPermissionResult: (Boolean) -> Unit,
    hasCameraPermission: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    // 1. launcher de permissão
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            onPermissionResult(granted)
        }

    // dispara o pedido uma vez, quando a tela entra em composição (só se ainda não tiver permissão)
    LaunchedEffect(Unit) {
        val alreadyGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            onPermissionResult(true)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // liga a câmera assim que (e sempre que) a permissão estiver concedida
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()
            val cameraPreview =
                CameraPreview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                cameraPreview,
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            InventraTopBar(
                navigationIcon = {
                    InventraBackButton(onClick = onBackClick)
                },
                title = {
                    Text(
                        "Câmera",
                        color = Color.White,
                        fontFamily = Montserrat,
                        fontSize = 17.sp,
                    )
                },
                actions = {
                    Text(
                        "⚡",
                        fontSize = 20.sp,
                        modifier = Modifier.clickable(onClick = onFlashClick),
                    )
                },
                modifier = Modifier.height(100.dp),
            )
            Box(modifier = Modifier.weight(1f)) {
                InventraScanOverlay(
                    instruction = instruction,
                    onScan = onScan,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
fun ScanRoute(
    navigator: Navigator,
    viewModel: ScanViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    ScanScreen(
        instruction = uiState.instruction,
        onBackClick = { navigator.back() },
        onFlashClick = viewModel::onFlashClick,
        onScan = viewModel::onScan,
        modifier = modifier,
        onPermissionResult = viewModel::onPermissionResult,
        hasCameraPermission = uiState.hasCameraPermission,
    )
}

@Preview
@Composable
private fun ScanScreenPreview() {
    ScanScreen(
        instruction = "Posicione o código de barras na moldura",
        onBackClick = {},
        onFlashClick = {},
        onScan = {},
        onPermissionResult = {},
        hasCameraPermission = false,
    )
}
