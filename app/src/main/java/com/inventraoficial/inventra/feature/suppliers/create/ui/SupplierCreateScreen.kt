package com.inventraoficial.inventra.feature.suppliers.create.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.navigation.Screen
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.Montserrat

private const val MAX_RATING = 5

@Composable
fun SupplierCreateScreen(
    uiState: SupplierCreateUiState,
    actions: SupplierCreateActions,
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
            title = { Text("Novo fornecedor", color = Color.White, fontFamily = Montserrat, fontSize = 17.sp) },
        )
        val contentModifier = Modifier.weight(1f).fillMaxWidth()
        val created = uiState.created
        if (created != null) {
            CreatedContent(created = created, actions = actions, modifier = contentModifier)
        } else {
            FormContent(uiState = uiState, actions = actions, modifier = contentModifier)
        }
    }
}

@Composable
private fun FormContent(
    uiState: SupplierCreateUiState,
    actions: SupplierCreateActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        InventraLabeledField(
            label = "Razão social",
            value = uiState.legalName,
            onValueChange = actions.onLegalNameChange,
            placeholder = "Ex.: Distribuidora Alimentos Ltda",
            modifier = Modifier.fillMaxWidth(),
        )
        InventraLabeledField(
            label = "CNPJ",
            value = uiState.cnpj,
            onValueChange = actions.onCnpjChange,
            keyboardType = KeyboardType.Number,
            placeholder = "Só os 14 números",
            modifier = Modifier.fillMaxWidth(),
        )
        InventraLabeledField(
            label = "E-mail (opcional)",
            value = uiState.email,
            onValueChange = actions.onEmailChange,
            keyboardType = KeyboardType.Email,
            placeholder = "contato@fornecedor.com",
            modifier = Modifier.fillMaxWidth(),
        )
        InventraLabeledField(
            label = "WhatsApp (opcional)",
            value = uiState.whatsapp,
            onValueChange = actions.onWhatsappChange,
            keyboardType = KeyboardType.Phone,
            placeholder = "DDD + número, só os números",
            modifier = Modifier.fillMaxWidth(),
        )
        RatingField(rating = uiState.rating, onRatingSelect = actions.onRatingSelect)
        uiState.submitError?.let {
            Text(it, fontFamily = Montserrat, fontSize = 14.sp, color = InventraDanger)
        }
        InventraPrimaryButton(
            text = "Cadastrar fornecedor",
            onClick = actions.onSaveClick,
            isLoading = uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RatingField(
    rating: Int,
    onRatingSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Avaliação (opcional)", fontFamily = Montserrat, fontSize = 18.sp, color = InventraPurple)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (star in 1..MAX_RATING) {
                Text(
                    text = if (star <= rating) "★" else "☆",
                    fontSize = 32.sp,
                    color = InventraGold,
                    modifier = Modifier.clickable { onRatingSelect(star) },
                )
            }
        }
    }
}

@Composable
private fun CreatedContent(
    created: CreatedSupplier,
    actions: SupplierCreateActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(96.dp)
                    .background(InventraSuccess.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = InventraSuccess,
                modifier = Modifier.size(48.dp),
            )
        }
        Text("Fornecedor cadastrado", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            text =
                "${created.name} já pode ser escolhido no cadastro de produto e na entrada no estoque. " +
                    "Na aba Fornecedores, ele aparece depois da primeira entrega ou quando for vinculado " +
                    "a um produto da cozinha.",
            fontFamily = Montserrat,
            fontSize = 14.sp,
            color = InventraMuted,
            textAlign = TextAlign.Center,
        )
        InventraSecondaryButton(
            text = "Ver fornecedor",
            onClick = { actions.onSeeSupplierClick(created.id) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
        )
        InventraTextLink(text = "Concluir", onClick = actions.onBackClick)
    }
}

@Composable
fun SupplierCreateRoute(
    navigator: Navigator,
    viewModel: SupplierCreateViewModel = viewModel(factory = SupplierCreateViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    SupplierCreateScreen(
        uiState = uiState,
        actions =
            SupplierCreateActions(
                onLegalNameChange = viewModel::onLegalNameChange,
                onCnpjChange = viewModel::onCnpjChange,
                onEmailChange = viewModel::onEmailChange,
                onWhatsappChange = viewModel::onWhatsappChange,
                onRatingSelect = viewModel::onRatingSelect,
                onSaveClick = viewModel::onSaveClick,
                onBackClick = { navigator.back() },
                onSeeSupplierClick = { supplierId ->
                    navigator.back()
                    navigator.navigate(Screen.SupplierDetail(supplierId))
                },
            ),
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun SupplierCreateFormPreview() {
    SupplierCreateScreen(
        uiState = SupplierCreateUiState(legalName = "Atacadão", cnpj = "12345678000190", rating = 4),
        actions = SupplierCreateActions(),
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun SupplierCreateDonePreview() {
    SupplierCreateScreen(
        uiState = SupplierCreateUiState(created = CreatedSupplier(id = 1, name = "Atacadão")),
        actions = SupplierCreateActions(),
    )
}
