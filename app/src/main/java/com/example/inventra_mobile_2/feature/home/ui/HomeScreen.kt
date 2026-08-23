package com.example.inventra_mobile_2.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.R
import com.example.inventra_mobile_2.core.designsystem.atoms.InventraStatusIconVariant
import com.example.inventra_mobile_2.core.designsystem.molecules.InventraStatTone
import com.example.inventra_mobile_2.core.designsystem.molecules.InventraUrgency
import com.example.inventra_mobile_2.core.designsystem.organisms.InventraBottomBar
import com.example.inventra_mobile_2.core.designsystem.organisms.InventraPriorityAlertsSection
import com.example.inventra_mobile_2.core.designsystem.organisms.InventraPriorityItem
import com.example.inventra_mobile_2.core.designsystem.organisms.InventraQrScanFab
import com.example.inventra_mobile_2.core.designsystem.organisms.InventraStat
import com.example.inventra_mobile_2.core.designsystem.organisms.InventraStatGrid
import com.example.inventra_mobile_2.core.designsystem.organisms.InventraTopBar
import com.example.inventra_mobile_2.ui.theme.Montserrat

@Composable
fun HomeScreen(
    userName: String,
    dateText: String,
    priorityItems: List<InventraPriorityItem>,
    stats: List<InventraStat>,
    onQrScanClick: () -> Unit,
    onBellClick: () -> Unit,
    onStockClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
        ) {
            InventraTopBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Seja bem-vindo, $userName!",
                            color = Color.White,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            dateText,
                            color = Color.White.copy(alpha = 0.8f),
                            fontFamily = Montserrat,
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    Text("🔔", fontSize = 20.sp, modifier = Modifier.clickable(onClick = onBellClick))
                }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                InventraPriorityAlertsSection(items = priorityItems)
                InventraStatGrid(stats = stats, modifier = Modifier.padding(top = 20.dp))
            }
            InventraBottomBar(
                onHomeClick = {},
                onStockClick = onStockClick,
                onHistoryClick = onHistoryClick,
                onNotificationsClick = onNotificationsClick
            )
        }
        InventraQrScanFab(
            onClick = onQrScanClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp)
                .offset(y = (-90).dp)
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        userName = "Jones",
        dateText = "Quinta, 2 de julho de 2026",
        priorityItems = listOf(
            InventraPriorityItem(
                painter = painterResource(R.drawable.ic_inventra_logo),
                title = "Arroz Camil",
                statusText = "Vence em 2 dias",
                urgency = InventraUrgency.Normal
            ),
            InventraPriorityItem(
                painter = painterResource(R.drawable.ic_inventra_logo),
                title = "Arroz Camil",
                statusText = "Vence em 2 dias",
                urgency = InventraUrgency.Normal
            )
        ),
        stats = listOf(
            InventraStat("Economia", "R$ 6767,00", "↑ 67%", InventraStatTone.Positive),
            InventraStat("Perdas", "R$ 6767,00", "↓ 67%", InventraStatTone.Negative)
        ),
        onQrScanClick = {},
        onBellClick = {},
        onStockClick = {},
        onHistoryClick = {},
        onNotificationsClick = {}
    )
}