package com.countertap.business.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.countertap.business.ui.theme.*
import com.countertap.business.viewmodel.AuthState
import com.countertap.business.viewmodel.AuthViewModel

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onSignInSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App title
            Text(
                text = "CounterTap",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBlue
            )
            Text(
                text = "Business",
                fontSize = 18.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Manage your shop, orders\nand menu from one place.",
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Google Sign-In button
            Button(
                onClick = { viewModel.signInWithGoogle(context) },
                enabled = authState !is AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign in with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            // Error message
            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
