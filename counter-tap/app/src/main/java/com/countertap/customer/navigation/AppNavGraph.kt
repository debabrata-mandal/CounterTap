package com.countertap.customer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.countertap.customer.ui.auth.SignInScreen
import com.countertap.customer.ui.cart.CartScreen
import com.countertap.customer.ui.home.CustomerHomeScreen
import com.countertap.customer.ui.menu.MenuScreen
import com.countertap.customer.ui.order.OrderTrackingScreen
import com.countertap.customer.ui.orders.OrderHistoryScreen
import com.countertap.customer.ui.scanner.ScannerScreen
import com.countertap.customer.viewmodel.CartViewModel

@Composable
fun AppNavGraph(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.SIGN_IN) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SIGN_IN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            CustomerHomeScreen(
                onOpenMenu = { tenantId ->
                    // Navigate directly to menu for recent restaurants (no table picker)
                    navController.navigate(Routes.menu(tenantId))
                },
                onScanNew = {
                    navController.navigate(Routes.SCANNER)
                },
                onViewHistory = {
                    navController.navigate(Routes.ORDER_HISTORY)
                },
                onTrackOrder = { tenantId, orderId ->
                    navController.navigate(Routes.orderTracking(tenantId, orderId))
                }
            )
        }

        composable(Routes.ORDER_HISTORY) {
            OrderHistoryScreen(
                onOpenOrder = { tenantId, orderId ->
                    navController.navigate(Routes.orderTracking(tenantId, orderId))
                },
                onHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                },
                onScan = {
                    navController.navigate(Routes.SCANNER)
                }
            )
        }

        composable(Routes.SCANNER) {
            ScannerScreen(
                onScanned = { tenantId ->
                    navController.navigate(Routes.menu(tenantId)) {
                        popUpTo(Routes.SCANNER) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.MENU,
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType })
        ) { menuEntry ->
            val tenantId = menuEntry.arguments?.getString("tenantId") ?: return@composable
            val cartViewModel: CartViewModel = hiltViewModel(menuEntry)
            MenuScreen(
                tenantId = tenantId,
                onGoToCart = { navController.navigate(Routes.cart(tenantId)) },
                onChangeRestaurant = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                },
                cartViewModel = cartViewModel
            )
        }

        composable(
            route = Routes.CART,
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType })
        ) { cartEntry ->
            val tenantId = cartEntry.arguments?.getString("tenantId") ?: return@composable
            val menuEntry = remember(cartEntry) {
                navController.getBackStackEntry(Routes.menu(tenantId))
            }
            val cartViewModel: CartViewModel = hiltViewModel(menuEntry)
            CartScreen(
                tenantId = tenantId,
                onBack = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    navController.navigate(Routes.orderTracking(tenantId, orderId)) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                },
                viewModel = cartViewModel
            )
        }

        composable(
            route = Routes.ORDER_TRACKING,
            arguments = listOf(
                navArgument("tenantId") { type = NavType.StringType },
                navArgument("orderId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getString("tenantId") ?: ""
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderTrackingScreen(
                tenantId = tenantId,
                orderId = orderId
            )
        }
    }
}
