package com.countertap.business.ui.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.countertap.business.navigation.Routes
import com.countertap.business.ui.theme.AccentBlue
import com.countertap.business.viewmodel.TenantState
import com.countertap.business.viewmodel.TenantViewModel

@Composable
fun LoadingScreen(navController: NavController) {
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

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AccentBlue)
    }
}
