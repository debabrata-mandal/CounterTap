package com.countertap.business

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import com.countertap.business.navigation.AppNavGraph
import com.countertap.business.navigation.Routes
import com.countertap.business.ui.theme.CounterTapTheme
import com.countertap.business.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CounterTapTheme {
                Surface {
                    val startDestination = if (authViewModel.currentUser != null) {
                        Routes.LOADING  // already signed in — check for tenant doc
                    } else {
                        Routes.SIGN_IN
                    }
                    AppNavGraph(startDestination = startDestination)
                }
            }
        }
    }
}
