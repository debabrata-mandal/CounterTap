package com.countertap.customer.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.countertap.customer.ui.theme.TextPrimary

@Composable
fun HomeScreen() {
    // TODO Phase 2: QR scan, menu browse, place order
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Home — Coming soon", color = TextPrimary)
    }
}
