package com.countertap.customer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import com.countertap.customer.navigation.AppNavGraph
import com.countertap.customer.navigation.Routes
import com.countertap.customer.ui.theme.CounterTapTheme
import com.countertap.customer.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !authViewModel.isReady.value }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CounterTapTheme {
                Surface {
                    val startDestination = if (authViewModel.currentUser != null) {
                        Routes.HOME
                    } else {
                        Routes.SIGN_IN
                    }
                    AppNavGraph(startDestination = startDestination)
                }
            }
        }
    }
}
