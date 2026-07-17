package com.countertap.customer.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.countertap.customer.ui.theme.AccentBlue
import com.countertap.customer.ui.theme.CardBackground
import com.countertap.customer.ui.theme.TextPrimary
import com.countertap.customer.ui.theme.TextSecondary

@Composable
fun CustomerBottomNav(
    selectedIndex: Int,
    onHome: () -> Unit,
    onOrders: () -> Unit,
    onScan: () -> Unit
) {
    data class NavTab(val label: String, val icon: ImageVector, val onClick: () -> Unit)

    val tabs = listOf(
        NavTab("Home", Icons.Default.Home, onHome),
        NavTab("My Orders", Icons.AutoMirrored.Filled.ReceiptLong, onOrders),
        NavTab("Scan", Icons.Default.QrCodeScanner, onScan)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = selectedIndex >= 0 && index == selectedIndex
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
                    .clickable { tab.onClick() }
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
