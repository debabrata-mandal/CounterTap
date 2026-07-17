package com.countertap.customer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.countertap.customer.repository.ShopRecord
import com.countertap.customer.ui.menu.MenuDisplayModeToggle
import com.countertap.customer.ui.theme.AccentBlue
import com.countertap.customer.ui.theme.BackgroundDark
import com.countertap.customer.ui.theme.CardBackground
import com.countertap.customer.ui.theme.DividerColor
import com.countertap.customer.ui.theme.ErrorRed
import com.countertap.customer.ui.theme.SuccessGreen
import com.countertap.customer.ui.theme.TextHint
import com.countertap.customer.ui.theme.TextPrimary
import com.countertap.customer.ui.theme.TextSecondary
import com.countertap.customer.ui.theme.WarningOrange
import com.countertap.customer.viewmodel.ActiveOrderInfo
import com.countertap.customer.viewmodel.CustomerHomeViewModel
import com.countertap.customer.viewmodel.MenuDisplayViewModel
import com.countertap.shared.OrderStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun CustomerHomeScreen(
    onOpenMenu: (tenantId: String) -> Unit,
    onScanNew: () -> Unit,
    onViewHistory: () -> Unit,
    onTrackOrder: (tenantId: String, orderId: String) -> Unit,
    onCreditLines: () -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: CustomerHomeViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.refresh() }
    val state by viewModel.state.collectAsState()
    val drawerState = rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CardBackground
            ) {
                // Drawer header
                Column(
                    modifier = Modifier.fillMaxWidth().background(BackgroundDark)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Box(
                        modifier = Modifier.size(52.dp).clip(CircleShape)
                            .background(AccentBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (currentUser?.displayName?.firstOrNull() ?: "U").toString().uppercase(),
                            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AccentBlue
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        currentUser?.displayName ?: "User",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                    )
                    if (currentUser?.email != null) {
                        Text(currentUser.email!!, fontSize = 12.sp, color = TextSecondary)
                    }
                }
                HorizontalDivider(color = DividerColor)
                Spacer(Modifier.height(8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = AccentBlue) },
                    label = { Text("My Credit Lines", color = TextPrimary) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCreditLines()
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )

                val menuDisplayViewModel: MenuDisplayViewModel = hiltViewModel()
                val menuDisplayMode by menuDisplayViewModel.mode.collectAsState()

                MenuDisplayModeToggle(
                    currentMode = menuDisplayMode,
                    onModeSelected = menuDisplayViewModel::setMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )

                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = DividerColor)

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = ErrorRed) },
                    label = { Text("Sign Out", color = ErrorRed) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSignOut()
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary, modifier = Modifier.size(24.dp))
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                            val greeting = when {
                                hour < 12 -> "Good morning"
                                hour < 17 -> "Good afternoon"
                                else -> "Good evening"
                            }
                            Text(greeting, fontSize = 12.sp, color = TextSecondary)
                            Text(
                                state.userName.ifBlank { "Welcome" },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        IconButton(onClick = onViewHistory) {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = "My Orders",
                                tint = AccentBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Active order cards
                if (state.activeOrders.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(WarningOrange)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "ACTIVE ORDERS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningOrange,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                    items(state.activeOrders, key = { it.orderId }) { order ->
                        ActiveOrderCard(
                            info = order,
                            onClick = { onTrackOrder(order.tenantId, order.orderId) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (state.recentShops.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(AccentBlue.copy(alpha = 0.12f))
                                ) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    "Scan a QR code to connect\nto your first restaurant",
                                    fontSize = 15.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(28.dp))
                                Button(
                                    onClick = onScanNew,
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Scan QR Code", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                } else {
                    // Recent Restaurants section header — accent bar pattern
                    item {
                        Row(
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(AccentBlue)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "RECENT RESTAURANTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }

                    items(state.recentShops, key = { it.tenantId }) { shop ->
                        ShopCard(shop = shop, onClick = { onOpenMenu(shop.tenantId) })
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        CustomerBottomNav(
            selectedIndex = 0,
            onHome = {},
            onOrders = onViewHistory,
            onScan = onScanNew
        )
    }
    } // end ModalNavigationDrawer content
}

@Composable
private fun ActiveOrderCard(info: ActiveOrderInfo, onClick: () -> Unit) {
    val statusColor = activeOrderStatusColor(info.status)
    val statusLabel = when (info.status) {
        OrderStatus.PENDING -> "Pending"
        OrderStatus.CONFIRMED -> "Confirmed"
        OrderStatus.PREPARING -> "Preparing"
        OrderStatus.READY -> "Ready!"
        else -> info.status
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Active order",
                fontSize = 10.sp,
                color = TextHint,
                letterSpacing = 1.sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(statusLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            info.shopName.ifBlank { "Your order" },
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(Modifier.height(12.dp))

        // Status timeline
        val currentStep = when (info.status) {
            OrderStatus.PENDING -> 0
            OrderStatus.CONFIRMED, OrderStatus.PREPARING -> 1
            OrderStatus.READY -> 2
            OrderStatus.COMPLETED -> 3
            else -> 0
        }

        val stepLabels = listOf("Placed", "Confirmed", "Ready", "Done")

        // Dots + connecting lines row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stepLabels.forEachIndexed { index, _ ->
                val isDone = index < currentStep
                val isCurrent = index == currentStep
                val dotColor = when {
                    isDone -> SuccessGreen
                    isCurrent -> AccentBlue
                    else -> DividerColor
                }
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                if (index < stepLabels.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.5.dp)
                            .background(if (isDone) SuccessGreen else DividerColor)
                    )
                }
            }
        }

        Spacer(Modifier.height(5.dp))

        // Labels row
        Row(modifier = Modifier.fillMaxWidth()) {
            stepLabels.forEachIndexed { index, label ->
                val isDone = index < currentStep
                val isCurrent = index == currentStep
                val color = when {
                    isDone -> SuccessGreen
                    isCurrent -> AccentBlue
                    else -> TextHint
                }
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    fontSize = 8.sp,
                    color = color,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    textAlign = when (index) {
                        0 -> TextAlign.Start
                        stepLabels.lastIndex -> TextAlign.End
                        else -> TextAlign.Center
                    }
                )
            }
        }
    }
}

private fun activeOrderStatusColor(status: String): Color = when (status) {
    OrderStatus.PENDING -> WarningOrange
    OrderStatus.CONFIRMED, OrderStatus.PREPARING -> AccentBlue
    OrderStatus.READY -> SuccessGreen
    OrderStatus.CANCELLED -> ErrorRed
    else -> TextHint
}


@Composable
private fun ShopCard(shop: ShopRecord, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(AccentBlue.copy(alpha = 0.7f))
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(start = 14.dp, top = 14.dp, bottom = 14.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentBlue.copy(alpha = 0.12f))
        ) {
            Icon(
                Icons.Default.Storefront,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Text(
                shop.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (shop.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    shop.address,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Button(
            onClick = onClick,
            modifier = Modifier.padding(end = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Order", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}
