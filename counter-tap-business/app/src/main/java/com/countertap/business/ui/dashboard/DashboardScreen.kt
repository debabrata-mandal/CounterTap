package com.countertap.business.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.countertap.business.ui.theme.AccentBlue
import com.countertap.business.ui.theme.BackgroundDark
import com.countertap.business.ui.theme.CardBackground
import com.countertap.business.ui.theme.ErrorRed
import com.countertap.business.ui.theme.SuccessGreen
import com.countertap.business.ui.theme.TextHint
import com.countertap.business.ui.theme.TextPrimary
import com.countertap.business.ui.theme.TextSecondary
import com.countertap.business.ui.theme.WarningOrange
import com.countertap.business.viewmodel.DashboardViewModel
import com.countertap.shared.Order
import com.countertap.shared.OrderStatus

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    color = AccentBlue,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            state.error != null -> {
                Text(
                    state.error!!,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Dashboard",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (state.shopName.isNotBlank()) {
                                    Text(
                                        state.shopName,
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            IconButton(onClick = {}) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = AccentBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Revenue section
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardBackground)
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                        ) {
                            Text(
                                "₹${state.todayRevenue}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "today's revenue",
                                fontSize = 12.sp,
                                color = TextHint
                            )
                        }
                    }

                    // Stat grid
                    item {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                StatCard(
                                    label = "PENDING",
                                    value = state.pendingCount.toString(),
                                    valueColor = WarningOrange
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                StatCard(
                                    label = "COMPLETED",
                                    value = state.completedCount.toString(),
                                    valueColor = SuccessGreen
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                StatCard(
                                    label = "TOTAL ORDERS",
                                    value = state.totalOrders.toString(),
                                    valueColor = AccentBlue
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                StatCard(
                                    label = "AVG ORDER",
                                    value = "₹${state.avgOrder}",
                                    valueColor = TextPrimary
                                )
                            }
                        }
                    }

                    // Active orders section
                    if (state.activeOrders.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
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
                                    "ACTIVE ORDERS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }

                        state.activeOrders.forEach { order ->
                            item(key = order.id) {
                                MiniOrderCard(order = order)
                            }
                        }
                    }

                    // Best sellers section
                    if (state.bestSellers.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(SuccessGreen)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "BEST SELLERS TODAY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    letterSpacing = 1.5.sp
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardBackground)
                            ) {
                                state.bestSellers.forEachIndexed { index, (name, qty) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            name,
                                            fontSize = 14.sp,
                                            color = TextPrimary,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "$qty sold",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AccentBlue
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // No orders today empty state
                    if (state.totalOrders == 0 && state.activeOrders.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No orders yet today",
                                    fontSize = 15.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(12.dp)
    ) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 9.sp, color = TextHint)
    }
}

@Composable
private fun MiniOrderCard(order: Order) {
    val accentColor = dashboardStatusColor(order.status)
    val itemsSummary = order.items.joinToString(", ") { "${it.quantity}× ${it.productName}" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColor)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    order.customerName.ifBlank { "Customer" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "₹${order.totalAmount.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )
                    Spacer(Modifier.width(8.dp))
                    DashboardStatusBadge(order.status)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                itemsSummary,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DashboardStatusBadge(status: String) {
    val (label, color) = when (status) {
        OrderStatus.PENDING -> "Pending" to WarningOrange
        OrderStatus.CONFIRMED -> "Confirmed" to AccentBlue
        OrderStatus.PREPARING -> "Preparing" to AccentBlue
        OrderStatus.READY -> "Ready" to SuccessGreen
        OrderStatus.COMPLETED -> "Done" to TextHint
        OrderStatus.CANCELLED -> "Cancelled" to ErrorRed
        else -> status to TextHint
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

private fun dashboardStatusColor(status: String): Color = when (status) {
    OrderStatus.PENDING -> WarningOrange
    OrderStatus.CONFIRMED, OrderStatus.PREPARING -> AccentBlue
    OrderStatus.READY -> SuccessGreen
    OrderStatus.CANCELLED -> ErrorRed
    else -> TextHint
}
