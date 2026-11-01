package com.inventraoficial.inventra.feature.stock.productform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraProductImage
import com.inventraoficial.inventra.feature.profile.photo.ui.ProfilePhotoSheet
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

data class ProductPhotoState(
    val url: String? = null,
    val isSheetVisible: Boolean = false,
    val isUploading: Boolean = false,
    val errorMessage: String? = null,
)

data class ProductPhotoActions(
    val onPhotoClick: () -> Unit = {},
    val onSheetDismiss: () -> Unit = {},
    val onPickClick: () -> Unit = {},
    val onCameraClick: () -> Unit = {},
    val onRemoveClick: () -> Unit = {},
)

@Composable
fun productPhotoActions(
    onPhotoClick: () -> Unit,
    onSheetDismiss: () -> Unit,
    onPhotoPicked: (String?) -> Unit,
    onCameraDenied: () -> Unit,
    onRemoveClick: () -> Unit,
): ProductPhotoActions {
    val photoPicker =
        rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
            onPhotoPicked(uri?.toString())
        }
    val openCamera = rememberProductCamera(onPhotoTaken = onPhotoPicked, onPermissionDenied = onCameraDenied)
    return ProductPhotoActions(
        onPhotoClick = onPhotoClick,
        onSheetDismiss = onSheetDismiss,
        onPickClick = {
            onSheetDismiss()
            photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        },
        onCameraClick = {
            onSheetDismiss()
            openCamera()
        },
        onRemoveClick = onRemoveClick,
    )
}

@Composable
fun ProductPhotoOptions(
    photo: ProductPhotoState,
    actions: ProductPhotoActions,
) {
    if (photo.isSheetVisible) {
        ProfilePhotoSheet(
            hasPhoto = photo.url != null,
            onPickClick = actions.onPickClick,
            onRemoveClick = actions.onRemoveClick,
            onDismiss = actions.onSheetDismiss,
            onCameraClick = actions.onCameraClick,
        )
    }
}

@Composable
fun ProductPhotoField(
    photo: ProductPhotoState,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clickable(enabled = !photo.isUploading, onClick = onClick),
            contentAlignment = Alignment.BottomEnd,
        ) {
            InventraProductImage(
                photoUrl = photo.url,
                shape = RoundedCornerShape(16.dp),
                iconSize = 48.dp,
                modifier = Modifier.fillMaxSize(),
            )
            Row(
                modifier =
                    Modifier
                        .padding(10.dp)
                        .background(InventraPurple, RoundedCornerShape(percent = 50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (photo.isUploading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = photo.label(),
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontSize = 13.sp,
                )
            }
        }
        photo.errorMessage?.let {
            Text(it, fontFamily = Montserrat, fontSize = 13.sp, color = InventraDanger)
        }
    }
}

private fun ProductPhotoState.label(): String =
    when {
        isUploading -> "Enviando…"
        url == null -> "Adicionar foto"
        else -> "Trocar foto"
    }
