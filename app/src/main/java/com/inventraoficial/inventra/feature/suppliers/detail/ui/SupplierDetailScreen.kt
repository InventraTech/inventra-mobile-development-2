package com.inventraoficial.inventra.feature.suppliers.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBadge
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraStarRating
import com.inventraoficial.inventra.core.designsystem.atoms.InventraThumbnail
import com.inventraoficial.inventra.core.designsystem.molecules.InventraContactRow
import com.inventraoficial.inventra.core.designsystem.molecules.InventraDeliveryHistoryRow
import com.inventraoficial.inventra.core.designsystem.molecules.InventraSupplierProductRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScreenContent
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun SupplierDetailScreen(
    imageRes: Int,
    name: String,
    rating: Int,
    badgeText: String,
    phone: String?,
    email: String?,
    products: List<SupplierProduct>,
    deliveryHistory: List<DeliveryHistoryEntry>,
    showsDeliveries: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            navigationIcon = {
                InventraBackButton(onClick = onBackClick)
            },
            title = {
                Text(
                    "Fornecedor",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
            },
        )
        InventraScreenContent {
            when {
                isLoading -> CenteredLoading(modifier = Modifier.weight(1f))
                errorMessage != null ->
                    ErrorState(message = errorMessage, onRetryClick = onRetryClick, modifier = Modifier.weight(1f))
                else ->
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            InventraThumbnail(
                                painter = painterResource(imageRes),
                                size = 64.dp,
                                contentScale = ContentScale.Fit,
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = name,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    color = InventraPurple,
                                    fontSize = 18.sp,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    InventraStarRating(rating = rating)
                                    InventraBadge(text = badgeText)
                                }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionTitle(text = "Contato")
                            if (phone == null && email == null) EmptyText(text = "Nenhum contato cadastrado.")
                            phone?.let { InventraContactRow(icon = Icons.Default.Call, text = it) }
                            email?.let { InventraContactRow(icon = Icons.Default.Email, text = it) }
                        }
                        if (showsDeliveries) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SectionTitle(text = "Produtos fornecidos")
                                if (products.isEmpty()) EmptyText(text = "Nenhum produto entregue nesta cozinha.")
                                products.forEach { product ->
                                    InventraSupplierProductRow(
                                        name = product.name,
                                        infoText = product.infoText,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SectionTitle(text = "Histórico de entregas")
                                if (deliveryHistory.isEmpty()) EmptyText(text = "Nenhuma entrega registrada.")
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    deliveryHistory.forEach { entry ->
                                        InventraDeliveryHistoryRow(
                                            date = entry.date,
                                            productName = entry.productName,
                                            quantityLabel = entry.quantityLabel,
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun EmptyText(text: String) {
    Text(
        text = text,
        fontFamily = Montserrat,
        fontSize = 14.sp,
        color = InventraMuted,
    )
}

@Composable
private fun CenteredLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = InventraPurple)
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            fontFamily = Montserrat,
            fontSize = 15.sp,
            color = InventraInk,
            textAlign = TextAlign.Center,
        )
        InventraSecondaryButton(text = "Tentar novamente", onClick = onRetryClick)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    )
}

@Composable
fun SupplierDetailRoute(
    supplierId: Int,
    navigator: Navigator,
    viewModel: SupplierDetailViewModel =
        viewModel(key = "supplier-detail-$supplierId", factory = SupplierDetailViewModel.factory(supplierId)),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    SupplierDetailScreen(
        imageRes = uiState.imageRes,
        name = uiState.name,
        rating = uiState.rating,
        badgeText = uiState.badgeText,
        phone = uiState.phone,
        email = uiState.email,
        products = uiState.products,
        deliveryHistory = uiState.deliveryHistory,
        showsDeliveries = uiState.showsDeliveries,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onBackClick = { navigator.back() },
        onRetryClick = viewModel::onRetryClick,
        modifier = modifier,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun SupplierDetailScreenPreview() {
    SupplierDetailScreen(
        imageRes = R.drawable.ic_inventra_logo,
        name = "Seara Alimentos",
        rating = 5,
        badgeText = "02.914.460/0001-50",
        phone = "(11) 4002-8922",
        email = "contato@seara.com.br",
        products =
            listOf(
                SupplierProduct(name = "Arroz Camil", infoText = "Último preço R\$ 7,00"),
                SupplierProduct(name = "Feijão Carioca", infoText = "Sem preço informado"),
            ),
        deliveryHistory =
            listOf(
                DeliveryHistoryEntry(date = "14/08/2026", productName = "Arroz Camil", quantityLabel = "Qtd. 12"),
            ),
        showsDeliveries = true,
        isLoading = false,
        errorMessage = null,
        onBackClick = {},
        onRetryClick = {},
    )
}
