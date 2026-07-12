package com.countertap.business.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.countertap.business.ui.dashboard.DashboardScreen
import com.countertap.business.ui.menu.ProductListScreen
import com.countertap.business.ui.orders.OrdersScreen
import com.countertap.business.ui.qr.QrCodeScreen
import com.countertap.business.ui.tables.TableManagementScreen
import com.countertap.business.ui.theme.AccentBlue
import com.countertap.business.ui.theme.BackgroundDark
import com.countertap.business.ui.theme.CardBackground
import com.countertap.business.ui.theme.CardElevated
import com.countertap.business.ui.theme.DividerColor
import com.countertap.business.ui.theme.TextHint
import com.countertap.business.ui.theme.TextPrimary
import com.countertap.business.ui.theme.TextSecondary
import com.countertap.business.viewmodel.AuthViewModel
import com.countertap.business.viewmodel.TenantState
import com.countertap.business.viewmodel.TenantViewModel
import com.countertap.shared.Product
import kotlinx.coroutines.launch

private data class TabItem(val label: String, val icon: ImageVector)

private val TABS = listOf(
    TabItem("Home", Icons.Default.Dashboard),
    TabItem("Orders", Icons.AutoMirrored.Filled.ReceiptLong),
    TabItem("Menu", Icons.AutoMirrored.Filled.List),
    TabItem("Tables", Icons.Default.TableRestaurant),
    TabItem("QR", Icons.Outlined.QrCode2)
)

@Composable
fun HomeScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onManageCategories: () -> Unit,
    onEditShop: () -> Unit,
    onEditUpi: () -> Unit,
    onCreditLines: () -> Unit,
    onSignOut: () -> Unit,
    tenantViewModel: TenantViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val tenantState by tenantViewModel.tenantState.collectAsState()
    val shopName = (tenantState as? TenantState.Ready)?.shop?.name ?: ""
    val ownerEmail = authViewModel.currentUser?.email ?: ""
    val ownerName = authViewModel.currentUser?.displayName ?: ownerEmail

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CardBackground,
                modifier = Modifier.width(300.dp)
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardElevated)
                        .statusBarsPadding()
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(AccentBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = shopName.ifEmpty { "Your Shop" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = ownerEmail,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))

                DrawerItem(
                    icon = Icons.Default.Edit,
                    label = "Edit Shop Details",
                    onClick = {
                        scope.launch { drawerState.close() }
                        onEditShop()
                    }
                )
                DrawerItem(
                    icon = Icons.Default.Payment,
                    label = "Edit UPI ID",
                    onClick = {
                        scope.launch { drawerState.close() }
                        onEditUpi()
                    }
                )
                DrawerItem(
                    icon = Icons.Default.AccountBalance,
                    label = "Credit Lines",
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCreditLines()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = DividerColor
                )

                DrawerItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    label = "Sign Out",
                    tint = TextSecondary,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSignOut()
                    }
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "CounterTap Business",
                    fontSize = 11.sp,
                    color = TextHint,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                )
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
            // Top bar with hamburger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Open menu",
                        tint = AccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = shopName.ifEmpty { "CounterTap" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> DashboardScreen(onNavigateToOrders = { selectedTab = 1 })
                    1 -> OrdersScreen()
                    2 -> ProductListScreen(
                        onAddProduct = onAddProduct,
                        onEditProduct = onEditProduct,
                        onManageCategories = onManageCategories
                    )
                    3 -> TableManagementScreen()
                    4 -> QrCodeScreen()
                }
            }

            PremiumNavBar(
                tabs = TABS,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(text = label, fontSize = 15.sp, color = tint, fontWeight = FontWeight.Medium)
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
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = if (isSelected)
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    else
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
