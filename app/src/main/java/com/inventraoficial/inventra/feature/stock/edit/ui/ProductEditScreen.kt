package com.inventraoficial.inventra.feature.stock.edit.ui

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraProductImage
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.organisms.InventraPickerOption
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.feature.profile.photo.ui.ProfilePhotoSheet
import com.inventraoficial.inventra.feature.stock.productform.ProductFormActions
import com.inventraoficial.inventra.feature.stock.productform.ProductFormFields
import com.inventraoficial.inventra.feature.stock.productform.ProductFormState
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun ProductEditScreen(
    uiState: ProductEditUiState,
    actions: ProductEditActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            navigationIcon = { InventraBackButton(onClick = actions.onBackClick) },
            title = { Text("Editar produto", color = Color.White, fontFamily = Montserrat, fontSize = 17.sp) },
        )
        val contentModifier = Modifier.weight(1f).fillMaxWidth()
        when {
            uiState.isLoading ->
                Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = InventraPurple)
                }
            uiState.errorMessage != null ->
                Column(
                    modifier = contentModifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(uiState.errorMessage, fontFamily = Montserrat, fontSize = 15.sp, textAlign = TextAlign.Center)
                    InventraSecondaryButton(text = "Tentar novamente", onClick = actions.onRetryClick)
                }
            else ->
                Column(
                    modifier =
                        contentModifier
                            .imePadding()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    ProductPhotoField(photo = uiState.photo, onClick = actions.photo.onPhotoClick)
                    ProductFormFields(form = uiState.form, actions = actions.form)
                    uiState.submitError?.let {
                        Text(it, fontFamily = Montserrat, fontSize = 14.sp, color = InventraDanger)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InventraSecondaryButton(
                            text = "Cancelar",
                            onClick = actions.onBackClick,
                            modifier = Modifier.weight(1f),
                        )
                        InventraPrimaryButton(
                            text = "Salvar",
                            onClick = actions.onSaveClick,
                            isLoading = uiState.isSubmitting,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
        }
    }

    if (uiState.photo.isSheetVisible) {
        ProfilePhotoSheet(
            hasPhoto = uiState.photo.url != null,
            onPickClick = actions.photo.onPickClick,
            onRemoveClick = actions.photo.onRemoveClick,
            onDismiss = actions.photo.onSheetDismiss,
            onCameraClick = actions.photo.onCameraClick,
        )
    }
}

@Composable
private fun ProductPhotoField(
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
                    text = if (photo.isUploading) "Enviando…" else "Trocar foto",
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

@Composable
fun ProductEditRoute(
    productId: Int,
    navigator: Navigator,
    viewModel: ProductEditViewModel =
        viewModel(key = "product-edit-$productId", factory = ProductEditViewModel.factory(productId)),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    val photoPicker =
        rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
            viewModel.onPhotoPicked(uri?.toString())
        }
    val openCamera =
        rememberProductCamera(
            onPhotoTaken = viewModel::onPhotoPicked,
            onPermissionDenied = viewModel::onCameraDenied,
        )

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) navigator.back()
    }

    ProductEditScreen(
        uiState = uiState,
        actions =
            ProductEditActions(
                form = viewModel.formActions,
                photo =
                    ProductPhotoActions(
                        onPhotoClick = viewModel::onPhotoClick,
                        onSheetDismiss = viewModel::onPhotoSheetDismiss,
                        onPickClick = {
                            viewModel.onPhotoSheetDismiss()
                            photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                        },
                        onCameraClick = {
                            viewModel.onPhotoSheetDismiss()
                            openCamera()
                        },
                        onRemoveClick = viewModel::onRemovePhotoClick,
                    ),
                onSaveClick = viewModel::onSaveClick,
                onRetryClick = viewModel::onRetryClick,
                onBackClick = { navigator.back() },
            ),
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun ProductEditScreenPreview() {
    ProductEditScreen(
        uiState =
            ProductEditUiState(
                isLoading = false,
                form =
                    ProductFormState(
                        name = "Ketchup",
                        brand = "Heinz",
                        barcode = "7896102000122",
                        units = listOf(InventraPickerOption(id = 6, title = "Unidade", subtitle = "UN")),
                        unitId = 6,
                        minStock = "4",
                        suppliers = listOf(InventraPickerOption(id = 2, title = "Atacadão")),
                        supplierId = 2,
                        referencePrice = "12,50",
                    ),
                photo = ProductPhotoState(isUploading = true),
            ),
        actions =
            ProductEditActions(
                form = ProductFormActions(),
                onSaveClick = {},
                onRetryClick = {},
                onBackClick = {},
            ),
    )
}
