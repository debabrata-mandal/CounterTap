package com.countertap.customer.ui.orders

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.countertap.customer.model.UserOrderSummary
import com.countertap.customer.ui.home.CustomerBottomNav
import com.countertap.customer.ui.theme.AccentBlue
import com.countertap.customer.ui.theme.BackgroundDark
import com.countertap.customer.ui.theme.CardBackground
import com.countertap.customer.ui.theme.ErrorRed
import com.countertap.customer.ui.theme.SuccessGreen
import com.countertap.customer.ui.theme.TextHint
import com.countertap.customer.ui.theme.TextPrimary
import com.countertap.customer.ui.theme.TextSecondary
import com.countertap.customer.ui.theme.WarningOrange
import com.countertap.customer.viewmodel.OrderHistoryViewModel
import com.countertap.shared.OrderStatus
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrderHistoryScreen(
    onOpenOrder: (tenantId: String, orderId: String) -> Unit,
    onHome: () -> Unit,
    onScan: () -> Unit,
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Header
        Column(
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding()
        ) {
        Text(
            "My Orders",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlue)
            }

            orders.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        "No orders yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your order history will appear here",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderHistoryCard(
                        order = order,
                        onClick = { onOpenOrder(order.tenantId, order.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
        } // end weight(1f) Column

        CustomerBottomNav(
            selectedIndex = 1,
            onHome = onHome,
            onOrders = {},
            onScan = onScan
        )
    }
}

@Composable
private fun OrderHistoryCard(
    order: UserOrderSummary,
    onClick: () -> Unit
) {
    val accentColor = orderStatusColor(order.status)
    val dateStr = order.createdAt?.let {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(it)
    } ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
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
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            // Shop name + status badge row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    order.shopName.ifBlank { "Restaurant" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                OrderStatusBadge(order.status)
            }

            // Date
            if (dateStr.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    dateStr,
                    fontSize = 12.sp,
                    color = TextHint
                )
            }

            // Items summary
            Spacer(modifier = Modifier.height(8.dp))
            val itemSummary = buildItemSummary(order)
            Text(
                itemSummary,
                fontSize = 13.sp,
                color = TextSecondary
            )

            // Total
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    "₹${order.totalAmount.toInt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )
            }
        }
    }
}

private fun buildItemSummary(order: UserOrderSummary): String {
    val items = order.items
    if (items.isEmpty()) return ""
    val shown = items.take(2).joinToString(", ") { "${it.quantity}× ${it.productName}" }
    return if (items.size > 2) "$shown + ${items.size - 2} more" else shown
}

@Composable
private fun OrderStatusBadge(status: String) {
    val (label, color) = when (status) {
        OrderStatus.PENDING -> "Pending" to WarningOrange
        OrderStatus.CONFIRMED -> "Confirmed" to AccentBlue
        OrderStatus.PREPARING -> "Preparing" to AccentBlue
        OrderStatus.READY -> "Ready" to SuccessGreen
        OrderStatus.COMPLETED -> "Completed" to TextHint
        OrderStatus.CANCELLED -> "Cancelled" to ErrorRed
        else -> status to TextHint
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

private fun orderStatusColor(status: String): Color = when (status) {
    OrderStatus.PENDING -> WarningOrange
    OrderStatus.CONFIRMED, OrderStatus.PREPARING -> AccentBlue
    OrderStatus.READY -> SuccessGreen
    OrderStatus.CANCELLED -> ErrorRed
    else -> TextHint
}
