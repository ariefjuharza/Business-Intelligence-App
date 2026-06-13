package studio.bonodigital.businessintelligence.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import studio.bonodigital.businessintelligence.data.repository.BiRepository
import studio.bonodigital.businessintelligence.ui.screens.IhsgDashboardScreen
import studio.bonodigital.businessintelligence.ui.screens.SettingsScreen
import studio.bonodigital.businessintelligence.ui.screens.StockAnalysisScreen
import studio.bonodigital.businessintelligence.ui.screens.WatchlistScreen
import studio.bonodigital.businessintelligence.ui.theme.DarkPrimary
import studio.bonodigital.businessintelligence.ui.theme.DarkSurface
import studio.bonodigital.businessintelligence.ui.theme.TextPrimary
import studio.bonodigital.businessintelligence.ui.theme.TextSecondary
import studio.bonodigital.businessintelligence.ui.viewmodel.BiViewModel
import studio.bonodigital.businessintelligence.ui.viewmodel.IhsgViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Ihsg : Screen("ihsg", "IHSG", Icons.Default.Home)
    object Analysis : Screen("analysis?ticker={ticker}", "Analisis", Icons.Default.Search) {
        fun createRoute(ticker: String) = "analysis?ticker=$ticker"
    }
    object Watchlist : Screen("watchlist", "Watchlist", Icons.Default.Star)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun MainNavigation(repository: BiRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val context = LocalContext.current
    
    // ViewModel yang berbagi repositori yang sama
    val biViewModel: BiViewModel = viewModel(
        factory = BiViewModel.provideFactory(context)
    )
    val ihsgViewModel: IhsgViewModel = viewModel(
        factory = IhsgViewModel.provideFactory(context)
    )

    val items = listOf(
        Screen.Ihsg,
        Screen.Analysis,
        Screen.Watchlist,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface
            ) {
                items.forEach { screen ->
                    val isSelected = when (screen) {
                        is Screen.Analysis -> currentRoute?.startsWith("analysis") == true
                        else -> currentRoute == screen.route
                    }
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            val routeToNavigate = when (screen) {
                                is Screen.Analysis -> "analysis?ticker="
                                else -> screen.route
                            }
                            navController.navigate(routeToNavigate) {
                                // Pop up ke tujuan awal grafik untuk
                                // menghindari penumpukan tumpukan tujuan yang besar
                                popUpTo(Screen.Ihsg.route) {
                                    saveState = true
                                }
                                // Hindari beberapa salinan dari tujuan yang sama saat
                                // memilih kembali item yang sama
                                launchSingleTop = true
                                // Pulihkan status saat memilih kembali item yang sebelumnya dipilih
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkPrimary,
                            selectedTextColor = TextPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = DarkSurface
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Ihsg.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Ihsg.route) {
                IhsgDashboardScreen(
                    viewModel = ihsgViewModel,
                    onStockClick = { ticker ->
                        navController.navigate(Screen.Analysis.createRoute(ticker))
                    }
                )
            }
            
            composable(
                route = Screen.Analysis.route,
                arguments = listOf(
                    navArgument("ticker") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val ticker = backStackEntry.arguments?.getString("ticker")
                StockAnalysisScreen(
                    viewModel = biViewModel,
                    initialTicker = ticker
                )
            }

            composable(Screen.Watchlist.route) {
                WatchlistScreen(
                    viewModel = biViewModel,
                    onTickerClick = { ticker ->
                        navController.navigate(Screen.Analysis.createRoute(ticker))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    repository = repository
                )
            }
        }
    }
}
