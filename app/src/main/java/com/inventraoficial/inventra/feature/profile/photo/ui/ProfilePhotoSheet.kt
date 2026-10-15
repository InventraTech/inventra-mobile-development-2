package com.inventraoficial.inventra.feature.profile.photo.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inventraoficial.inventra.core.designsystem.molecules.InventraMenuRow
import com.inventraoficial.inventra.ui.theme.InventraBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePhotoSheet(
    hasPhoto: Boolean,
    onPickClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = InventraBackground,
    ) {
        ProfilePhotoSheetContent(hasPhoto = hasPhoto, onPickClick = onPickClick, onRemoveClick = onRemoveClick)
    }
}

@Composable
fun ProfilePhotoSheetContent(
    hasPhoto: Boolean,
    onPickClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InventraMenuRow(
            icon = Icons.Default.PhotoLibrary,
            label = "Escolher da galeria",
            onClick = onPickClick,
        )
        if (hasPhoto) {
            InventraMenuRow(
                icon = Icons.Default.Delete,
                label = "Remover foto",
                onClick = onRemoveClick,
            )
        }
    }
}

@Composable
fun ProfilePhotoHost(
    uiState: ProfilePhotoUiState,
    viewModel: ProfilePhotoViewModel,
) {
    val photoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            viewModel.onPhotoPicked(uri?.toString())
        }

    if (uiState.isSheetVisible) {
        ProfilePhotoSheet(
            hasPhoto = uiState.hasPhoto,
            onPickClick = {
                viewModel.onSheetDismiss()
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onRemoveClick = viewModel::onRemoveClick,
            onDismiss = viewModel::onSheetDismiss,
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun ProfilePhotoSheetContentPreview() {
    ProfilePhotoSheetContent(hasPhoto = true, onPickClick = {}, onRemoveClick = {})
}
