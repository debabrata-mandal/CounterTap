package com.countertap.business.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.countertap.business.ui.menu.ProductListScreen
import com.countertap.business.ui.qr.QrCodeScreen
import com.countertap.business.ui.theme.TextPrimary
import com.countertap.shared.Product

@Composable
fun HomeScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onManageCategories: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {

        // Tab content fills all remaining space
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Orders — coming in Phase 4", color = TextPrimary)
                }
                1 -> ProductListScreen(
                    onAddProduct = onAddProduct,
                    onEditProduct = onEditProduct,
                    onManageCategories = onManageCategories
                )
                2 -> QrCodeScreen()
            }
        }

        // Bottom nav — zero internal insets, navigationBarsPadding adds space below
        NavigationBar(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
            windowInsets = WindowInsets(0)
        ) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Orders") },
                label = { Text("Orders") }
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Menu") },
                label = { Text("Menu") }
            )
            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(Icons.Default.QrCode, contentDescription = "QR Code") },
                label = { Text("QR Code") }
            )
        }
    }
}
