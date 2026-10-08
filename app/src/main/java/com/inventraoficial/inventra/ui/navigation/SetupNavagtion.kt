package com.inventraoficial.inventra.ui.navigation

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomBar
import com.inventraoficial.inventra.core.designsystem.organisms.InventraBottomDestination
import com.inventraoficial.inventra.core.designsystem.organisms.InventraMascotFab
import com.inventraoficial.inventra.feature.assistant.ui.ChatRoute
import com.inventraoficial.inventra.feature.auth.login.ui.LoginRoute
import com.inventraoficial.inventra.feature.auth.register.ui.RegisterRoute
import com.inventraoficial.inventra.feature.cozinha.create.ui.CreateCozinhaRoute
import com.inventraoficial.inventra.feature.cozinha.info.ui.CozinhaInfoRoute
import com.inventraoficial.inventra.feature.cozinha.requestsent.ui.RequestSentRoute
import com.inventraoficial.inventra.feature.cozinha.select.ui.SelectCozinhaRoute
import com.inventraoficial.inventra.feature.home.ui.HomeRoute
import com.inventraoficial.inventra.feature.notifications.ui.NotificationRoute
import com.inventraoficial.inventra.feature.profile.details.ui.ProfileDetailsRoute
import com.inventraoficial.inventra.feature.profile.members.ui.ManageMembersRoute
import com.inventraoficial.inventra.feature.profile.preferences.ui.AccountPreferencesRoute
import com.inventraoficial.inventra.feature.profile.ui.ProfileRoute
import com.inventraoficial.inventra.feature.requirements.history.ui.RequirementsHistoryRoute
import com.inventraoficial.inventra.feature.requirements.list.ui.RequirementsListRoute
import com.inventraoficial.inventra.feature.scan.ui.ScanRoute
import com.inventraoficial.inventra.feature.stock.detail.ui.StockDetailRoute
import com.inventraoficial.inventra.feature.stock.edit.ui.ProductEditRoute
import com.inventraoficial.inventra.feature.stock.list.ui.StockListRoute
import com.inventraoficial.inventra.feature.stock.stockout.ui.StockOutRoute
import com.inventraoficial.inventra.feature.suppliers.detail.ui.SupplierDetailRoute
import com.inventraoficial.inventra.feature.suppliers.list.ui.SupplierRoute

@Composable
fun SetupNavigation(startViewModel: StartViewModel = viewModel(factory = StartViewModel.Factory)) {
    val startScreen by startViewModel.startScreen.collectAsState()

    val screen = startScreen
    if (screen == null) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.White),
        )
    } else {
        AppNavigation(startScreen = screen)
    }
}

@Composable
private fun AppNavigation(startScreen: Screen) {
    val navigator =
        remember {
            Navigator(mutableStateListOf(startScreen))
        }

    val currentScreen = navigator.currentBackStack.last()
    val selectedTab = currentScreen.toBottomDestination()
    val isFullBleed = currentScreen.isFullBleed()
    val insetsModifier =
        if (selectedTab == null && !isFullBleed) Modifier.navigationBarsPadding() else Modifier

    SystemBarsIcons(
        darkStatusBarIcons = isFullBleed,
        darkNavigationBarIcons = selectedTab == null,
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (selectedTab != null) {
                InventraBottomBar(
                    selected = selectedTab,
                    onSelect = { destination ->
                        navigator.navigateTopLevel(destination.toScreen())
                    },
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == InventraBottomDestination.Home) {
                InventraMascotFab(
                    painter = painterResource(R.drawable.ic_mascote),
                    onClick = { navigator.navigate(Screen.Chat) },
                )
            }
        },
    ) { innerPadding ->
        NavDisplay(
            modifier = insetsModifier.padding(innerPadding),
            backStack = navigator.currentBackStack,
            onBack = { navigator.back() },
            entryProvider =
                entryProvider {
                    entry<Screen.Chat> {
                        ChatRoute(navigator = navigator)
                    }
                    entry<Screen.StockList> {
                        StockListRoute(navigator = navigator)
                    }
                    entry<Screen.Notifications> {
                        NotificationRoute(navigator = navigator)
                    }
                    entry<Screen.Suppliers> {
                        SupplierRoute(navigator = navigator)
                    }
                    entry<Screen.SupplierDetail> {
                        SupplierDetailRoute(navigator = navigator)
                    }
                    entry<Screen.Scan> {
                        ScanRoute(navigator = navigator)
                    }
                    entry<Screen.StockDetail> {
                        StockDetailRoute(navigator = navigator)
                    }
                    entry<Screen.ProductEdit> {
                        ProductEditRoute(navigator = navigator)
                    }
                    entry<Screen.StockOut> {
                        StockOutRoute(navigator = navigator)
                    }
                    entry<Screen.Home> {
                        HomeRoute(navigator = navigator)
                    }
                    entry<Screen.Login> {
                        LoginRoute(navigator = navigator)
                    }
                    entry<Screen.Register> {
                        RegisterRoute(navigator = navigator)
                    }
                    entry<Screen.CreateCozinha> {
                        CreateCozinhaRoute(navigator = navigator)
                    }
                    entry<Screen.SelectCozinha> {
                        SelectCozinhaRoute(navigator = navigator)
                    }
                    entry<Screen.RequestSent> {
                        RequestSentRoute(navigator = navigator)
                    }
                    entry<Screen.Profile> {
                        ProfileRoute(navigator = navigator)
                    }
                    entry<Screen.ProfileDetails> {
                        ProfileDetailsRoute(navigator = navigator)
                    }
                    entry<Screen.ManageMembers> {
                        ManageMembersRoute(navigator = navigator)
                    }
                    entry<Screen.AccountPreferences> {
                        AccountPreferencesRoute(navigator = navigator)
                    }
                    entry<Screen.CozinhaInfo> {
                        CozinhaInfoRoute(navigator = navigator)
                    }
                    entry<Screen.RequirementsList> {
                        RequirementsListRoute(navigator = navigator)
                    }
                    entry<Screen.RequirementsHistory> {
                        RequirementsHistoryRoute(navigator = navigator)
                    }
                },
        )
    }
}

/**
 * Ajusta a cor dos icones da barra de status e da barra de navegacao do sistema.
 * "Dark" quer dizer icones escuros, para fundos claros.
 */
@Composable
private fun SystemBarsIcons(
    darkStatusBarIcons: Boolean,
    darkNavigationBarIcons: Boolean,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = darkStatusBarIcons
            controller.isAppearanceLightNavigationBars = darkNavigationBarIcons
        }
    }
}
