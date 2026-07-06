package com.countertap.business

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.countertap.business.navigation.AppNavGraph
import com.countertap.business.navigation.Routes
import com.countertap.business.ui.theme.CounterTapTheme
import com.countertap.business.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !authViewModel.isReady.value }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }
        // Save FCM token
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener
            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
        }

        setContent {
            CounterTapTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
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
