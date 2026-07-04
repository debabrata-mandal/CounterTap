package com.countertap.business.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.countertap.business.ui.auth.SignInScreen
import com.countertap.business.ui.home.HomeScreen
import com.countertap.business.ui.onboarding.ShopSetupScreen
import com.countertap.business.ui.onboarding.UpiSetupScreen
import com.countertap.business.viewmodel.TenantState
import com.countertap.business.viewmodel.TenantViewModel

object Routes {
    const val SIGN_IN = "sign_in"
    const val LOADING = "loading"
    const val SHOP_SETUP = "shop_setup"
    const val UPI_SETUP = "upi_setup"
    const val HOME = "home"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SIGN_IN) {
            SignInScreen(
                onSignedIn = {
                    navController.navigate(Routes.LOADING) {
                        popUpTo(Routes.SIGN_IN) { inclusive = true }
                    }
                }
            )
        }

        // Tenant check after sign-in: routes to onboarding or home
        composable(Routes.LOADING) {
            val tenantViewModel: TenantViewModel = hiltViewModel()
            val tenantState by tenantViewModel.tenantState.collectAsState()

            LaunchedEffect(tenantState) {
                when (tenantState) {
                    is TenantState.NeedsOnboarding -> navController.navigate(Routes.SHOP_SETUP) {
                        popUpTo(Routes.LOADING) { inclusive = true }
                    }
                    is TenantState.Ready -> navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOADING) { inclusive = true }
                    }
                    else -> Unit
                }
            }
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
            HomeScreen()
        }
    }
}
