package com.countertap.customer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.countertap.customer.ui.auth.SignInScreen
import com.countertap.customer.ui.home.HomeScreen

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
            HomeScreen()
        }
    }
}
