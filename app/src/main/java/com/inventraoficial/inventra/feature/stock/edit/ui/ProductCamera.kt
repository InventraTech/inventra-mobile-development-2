package com.inventraoficial.inventra.feature.stock.edit.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File

@Composable
fun rememberProductCamera(
    onPhotoTaken: (String) -> Unit,
    onPermissionDenied: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    var pendingUri by rememberSaveable { mutableStateOf<String?>(null) }

    val camera =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
            val uri = pendingUri
            pendingUri = null
            if (saved && uri != null) onPhotoTaken(uri)
        }

    fun openCamera() {
        val uri = context.newCameraUri()
        pendingUri = uri
        camera.launch(uri.toUri())
    }

    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) openCamera() else onPermissionDenied()
        }

    return {
        val granted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) openCamera() else permission.launch(Manifest.permission.CAMERA)
    }
}

private fun Context.newCameraUri(): String {
    val folder = File(cacheDir, "camera").apply { mkdirs() }
    val file = File.createTempFile("produto-", ".jpg", folder)
    return FileProvider.getUriForFile(this, "$packageName.fileprovider", file).toString()
}
