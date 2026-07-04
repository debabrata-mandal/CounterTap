package com.countertap.business.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.countertap.business.ui.dashboard.DashboardScreen
import com.countertap.business.ui.menu.ProductListScreen
import com.countertap.business.ui.orders.OrdersScreen
import com.countertap.business.ui.qr.QrCodeScreen
import com.countertap.business.ui.theme.AccentBlue
import com.countertap.business.ui.theme.BackgroundDark
import com.countertap.business.ui.theme.CardBackground
import com.countertap.business.ui.theme.TextSecondary
import com.countertap.shared.Product

private data class TabItem(val label: String, val icon: ImageVector)

private val TABS = listOf(
    TabItem("Home", Icons.Default.Dashboard),
    TabItem("Orders", Icons.AutoMirrored.Filled.ReceiptLong),
    TabItem("Menu", Icons.AutoMirrored.Filled.List),
    TabItem("QR Code", Icons.Default.QrCode)
)

@Composable
fun HomeScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onManageCategories: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> DashboardScreen()
                1 -> OrdersScreen()
                2 -> ProductListScreen(
                    onAddProduct = onAddProduct,
                    onEditProduct = onEditProduct,
                    onManageCategories = onManageCategories
                )
                3 -> QrCodeScreen()
            }
        }

        PremiumNavBar(
            tabs = TABS,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}


@Composable
private fun PremiumNavBar(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedIndex
            val iconTint by animateColorAsState(
                targetValue = if (isSelected) AccentBlue else TextSecondary,
                label = "tint_$index"
            )
            val labelColor by animateColorAsState(
                targetValue = if (isSelected) AccentBlue else TextSecondary,
                label = "label_$index"
            )

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = if (isSelected)
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    else
                        Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = tab.label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = labelColor
                )
            }
        }
    }
}
