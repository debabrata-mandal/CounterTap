package com.countertap.customer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.countertap.customer.ui.auth.SignInScreen
import com.countertap.customer.ui.cart.CartScreen
import com.countertap.customer.ui.menu.MenuScreen
import com.countertap.customer.ui.order.OrderConfirmationScreen
import com.countertap.customer.ui.scanner.ScannerScreen
import com.countertap.customer.viewmodel.CartViewModel

@Composable
fun AppNavGraph(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.SIGN_IN) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Routes.SCANNER) {
                        popUpTo(Routes.SIGN_IN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SCANNER) {
            ScannerScreen(
                onScanned = { tenantId ->
                    navController.navigate(Routes.menu(tenantId))
                }
            )
        }

        composable(
            route = Routes.MENU,
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType })
        ) { menuEntry ->
            val tenantId = menuEntry.arguments?.getString("tenantId") ?: return@composable
            // CartViewModel scoped to this menu back-stack entry so it can be shared with cart
            val cartViewModel: CartViewModel = hiltViewModel(menuEntry)
            MenuScreen(
                tenantId = tenantId,
                onGoToCart = { navController.navigate(Routes.cart(tenantId)) },
                cartViewModel = cartViewModel
            )
        }

        composable(
            route = Routes.CART,
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType })
        ) { cartEntry ->
            val tenantId = cartEntry.arguments?.getString("tenantId") ?: return@composable
            // Retrieve the same CartViewModel from the menu entry
            val menuEntry = remember(cartEntry) {
                navController.getBackStackEntry(Routes.menu(tenantId))
            }
            val cartViewModel: CartViewModel = hiltViewModel(menuEntry)
            CartScreen(
                tenantId = tenantId,
                onBack = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    navController.navigate(Routes.orderConfirmed(orderId)) {
                        popUpTo(Routes.SCANNER) { inclusive = false }
                    }
                },
                viewModel = cartViewModel
            )
        }

        composable(
            route = Routes.ORDER_CONFIRMED,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderConfirmationScreen(
                orderId = orderId,
                onScanAnother = {
                    navController.navigate(Routes.SCANNER) {
                        popUpTo(Routes.SCANNER) { inclusive = true }
                    }
                }
            )
        }
    }
}
