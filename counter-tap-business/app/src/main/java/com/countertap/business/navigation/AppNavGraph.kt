package com.countertap.business.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.countertap.business.ui.auth.SignInScreen
import com.countertap.business.ui.credit.CreditScreen
import com.countertap.business.ui.home.HomeScreen
import com.countertap.business.ui.loading.LoadingScreen
import com.countertap.business.ui.menu.AddEditProductScreen
import com.countertap.business.ui.menu.CategoryScreen
import com.countertap.business.ui.onboarding.ShopSetupScreen
import com.countertap.business.ui.onboarding.UpiSetupScreen
import com.countertap.business.viewmodel.AuthViewModel
import com.countertap.business.viewmodel.MenuViewModel
import com.countertap.business.viewmodel.TenantViewModel

@Composable
fun AppNavGraph(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.SIGN_IN) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Routes.LOADING) {
                        popUpTo(Routes.SIGN_IN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOADING) {
            LoadingScreen(navController = navController)
        }

        composable(Routes.SHOP_SETUP) {
            ShopSetupScreen(
                onSetupComplete = {
                    navController.navigate(Routes.UPI_SETUP) {
                        popUpTo(Routes.SHOP_SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.UPI_SETUP) {
            UpiSetupScreen(
                onSetupComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.UPI_SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            val authViewModel: AuthViewModel = hiltViewModel()
            HomeScreen(
                onAddProduct = { navController.navigate(Routes.ADD_PRODUCT) },
                onEditProduct = { product -> navController.navigate(Routes.editProduct(product.id)) },
                onManageCategories = { navController.navigate(Routes.CATEGORIES) },
                onEditShop = { navController.navigate(Routes.EDIT_SHOP) },
                onEditUpi = { navController.navigate(Routes.EDIT_UPI) },
                onCreditLines = { navController.navigate(Routes.CREDIT_LINES) },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.EDIT_SHOP) { currentEntry ->
            val homeEntry = remember(currentEntry) { navController.getBackStackEntry(Routes.HOME) }
            val tenantViewModel: TenantViewModel = hiltViewModel(homeEntry)
            ShopSetupScreen(
                onSetupComplete = {},
                isEditMode = true,
                onBack = { navController.popBackStack() },
                viewModel = tenantViewModel
            )
        }

        composable(Routes.EDIT_UPI) { currentEntry ->
            val homeEntry = remember(currentEntry) { navController.getBackStackEntry(Routes.HOME) }
            val tenantViewModel: TenantViewModel = hiltViewModel(homeEntry)
            UpiSetupScreen(
                onSetupComplete = {},
                isEditMode = true,
                onBack = { navController.popBackStack() },
                viewModel = tenantViewModel
            )
        }

        composable(Routes.CREDIT_LINES) {
            CreditScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADD_PRODUCT) { currentEntry ->
            val homeEntry = remember(currentEntry) { navController.getBackStackEntry(Routes.HOME) }
            val menuViewModel: MenuViewModel = hiltViewModel(homeEntry)
            AddEditProductScreen(
                existingProduct = null,
                onDone = { navController.popBackStack() },
                viewModel = menuViewModel
            )
        }

        composable(Routes.EDIT_PRODUCT) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val homeEntry = remember(backStackEntry) { navController.getBackStackEntry(Routes.HOME) }
            val menuViewModel: MenuViewModel = hiltViewModel(homeEntry)
            val uiState by menuViewModel.uiState.collectAsState()
            val product = uiState.products.find { it.id == productId }
            AddEditProductScreen(
                existingProduct = product,
                onDone = { navController.popBackStack() },
                viewModel = menuViewModel
            )
        }

        composable(Routes.CATEGORIES) { currentEntry ->
            val homeEntry = remember(currentEntry) { navController.getBackStackEntry(Routes.HOME) }
            val menuViewModel: MenuViewModel = hiltViewModel(homeEntry)
            CategoryScreen(
                onBack = { navController.popBackStack() },
                viewModel = menuViewModel
            )
        }
    }
}
