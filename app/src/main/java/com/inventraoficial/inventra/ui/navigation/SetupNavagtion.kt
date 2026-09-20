package com.inventraoficial.inventra.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomBar
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomDestination
import com.inventraoficial.inventra.core.designsystem.organisms.InventraQrScanFab
import com.inventraoficial.inventra.feature.assistant.ui.ChatRoute
import com.inventraoficial.inventra.feature.home.ui.HomeRoute
import com.inventraoficial.inventra.feature.notifications.ui.NotificationRoute
import com.inventraoficial.inventra.feature.scan.ui.ScanRoute
import com.inventraoficial.inventra.feature.stock.detail.ui.StockDetailRoute
import com.inventraoficial.inventra.feature.stock.edit.ui.ProductEditRoute
import com.inventraoficial.inventra.feature.stock.list.ui.StockListRoute
import com.inventraoficial.inventra.feature.stock.stockout.ui.StockOutRoute
import com.inventraoficial.inventra.feature.suppliers.ui.SupplierRoute

@Composable
fun SetupNavigation() {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }

    val currentScreen = backStack.last()
    val selectedTab = currentScreen.toBottomDestination()

    Scaffold(
        bottomBar = {
            if (selectedTab != null) {
                InventraBottomBar(
                    selected = selectedTab,
                    onSelect = { destination ->
                        backStack.navigateTopLevel(destination.toScreen())
                    },
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == InventraBottomDestination.Stock || selectedTab == InventraBottomDestination.Home) {
                InventraQrScanFab(
                    onClick = { backStack.add(Screen.Scan) },
                )
            }
        },
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider =
                entryProvider {
                    entry<Screen.Chat> {
                        ChatRoute()
                    }
                    entry<Screen.StockList> {
                        StockListRoute()
                    }
                    entry<Screen.Notifications> {
                        NotificationRoute()
                    }
                    entry<Screen.Suppliers> {
                        SupplierRoute()
                    }
                    entry<Screen.Scan> {
                        ScanRoute()
                    }
                    entry<Screen.StockDetail> {
                        StockDetailRoute()
                    }
                    entry<Screen.ProductEdit> {
                        ProductEditRoute()
                    }
                    entry<Screen.StockOut> {
                        StockOutRoute()
                    }
                    entry<Screen.Home> {
                        HomeRoute(backStack = backStack)
                    }
                },
        )
    }
}
