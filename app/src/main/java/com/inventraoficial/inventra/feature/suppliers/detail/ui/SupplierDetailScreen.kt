package com.inventraoficial.inventra.feature.suppliers.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBadge
import com.inventraoficial.inventra.core.designsystem.atoms.InventraStarRating
import com.inventraoficial.inventra.core.designsystem.atoms.InventraThumbnail
import com.inventraoficial.inventra.core.designsystem.molecules.InventraContactRow
import com.inventraoficial.inventra.core.designsystem.molecules.InventraDeliveryHistoryRow
import com.inventraoficial.inventra.core.designsystem.molecules.InventraSupplierProductRow
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun SupplierDetailScreen(
    imageRes: Int,
    name: String,
    rating: Int,
    badgeText: String,
    phone: String,
    email: String,
    address: String,
    products: List<SupplierProduct>,
    deliveryHistory: List<DeliveryHistoryEntry>,
    onBackClick: () -> Unit,
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
                InventraContactRow(icon = Icons.Default.Call, text = phone)
                InventraContactRow(icon = Icons.Default.Email, text = email)
                InventraContactRow(icon = Icons.Default.LocationOn, text = address)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(text = "Produtos fornecidos")
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
    navigator: Navigator,
    viewModel: SupplierDetailViewModel = viewModel(),
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
        address = uiState.address,
        products = uiState.products,
        deliveryHistory = uiState.deliveryHistory,
        onBackClick = { navigator.back() },
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
        badgeText = "VPQ 50",
        phone = "(11) 4002-8922",
        email = "contato@seara.com.br",
        address = "Av. das Indústrias, 450 – São Paulo, SP",
        products =
            listOf(
                SupplierProduct(name = "Arroz Camil", infoText = "R\$7,00/un · 3 dias"),
                SupplierProduct(name = "Feijão Carioca", infoText = "R\$7,00/un · 3 dias"),
            ),
        deliveryHistory =
            listOf(
                DeliveryHistoryEntry(date = "14/08/2026", productName = "Arroz Camil", quantityLabel = "12 un"),
            ),
        onBackClick = {},
    )
}
