package com.countertap.business.ui.orders

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.countertap.business.ui.theme.AccentBlue
import com.countertap.business.ui.theme.BackgroundDark
import com.countertap.business.ui.theme.CardBackground
import com.countertap.business.ui.theme.CardElevated
import com.countertap.business.ui.theme.DividerColor
import com.countertap.business.ui.theme.ErrorRed
import com.countertap.business.ui.theme.SuccessGreen
import com.countertap.business.ui.theme.TextHint
import com.countertap.business.ui.theme.TextPrimary
import com.countertap.business.ui.theme.TextSecondary
import com.countertap.business.ui.theme.WarningOrange
import com.countertap.business.viewmodel.OrdersViewModel
import com.countertap.shared.Order
import com.countertap.shared.OrderStatus
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrdersScreen(viewModel: OrdersViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        Text(
            "Orders",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlue)
            }

            uiState.error != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.error!!, color = TextSecondary, fontSize = 14.sp)
            }

            uiState.activeOrders.isEmpty() && uiState.doneOrders.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No orders yet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("New orders will appear here in real time", fontSize = 14.sp, color = TextSecondary)
                }
            }

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (uiState.activeOrders.isNotEmpty()) {
                    item {
                        SectionHeader("ACTIVE", uiState.activeOrders.size)
                    }
                    items(uiState.activeOrders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onAccept = { viewModel.updateStatus(order.id, OrderStatus.CONFIRMED) },
                            onReject = { viewModel.updateStatus(order.id, OrderStatus.CANCELLED) },
                            onReady = { viewModel.updateStatus(order.id, OrderStatus.READY) },
                            onComplete = { viewModel.updateStatus(order.id, OrderStatus.COMPLETED) }
                        )
                    }
                }

                if (uiState.doneOrders.isNotEmpty()) {
                    item {
                        SectionHeader("COMPLETED / CANCELLED", uiState.doneOrders.size)
                    }
                    items(uiState.doneOrders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onAccept = {},
                            onReject = {},
                            onReady = {},
                            onComplete = {}
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String, count: Int) {
    Row(
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AccentBlue)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label  •  $count",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentBlue,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onReady: () -> Unit,
    onComplete: () -> Unit
) {
    val accentColor = statusColor(order.status)
    val timeStr = order.createdAt?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
    } ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
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
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "#${order.id.take(6).uppercase()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(order.status)
                }
                Text(timeStr, fontSize = 12.sp, color = TextHint)
            }

            // Customer
            if (order.customerName.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    order.customerName,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            // Items
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(8.dp))
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${item.quantity}×  ${item.productName}",
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "₹${(item.price * item.quantity).toInt()}",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }

            // Note
            if (order.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Note: ${order.note}",
                    fontSize = 12.sp,
                    color = TextHint,
                    fontWeight = FontWeight.Medium
                )
            }

            // Total
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", fontSize = 14.sp, color = TextSecondary)
                Text(
                    "₹${order.totalAmount.toInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )
            }

            // Actions
            val actions = actionsFor(order.status)
            if (actions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actions.forEach { action ->
                        when (action) {
                            Action.ACCEPT -> Button(
                                onClick = onAccept,
                                modifier = Modifier.weight(1f).height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Accept", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold) }

                            Action.REJECT -> OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                            ) { Text("Reject", fontSize = 13.sp, color = ErrorRed, fontWeight = FontWeight.Bold) }

                            Action.MARK_READY -> Button(
                                onClick = onReady,
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Mark Ready", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold) }

                            Action.COMPLETE -> Button(
                                onClick = onComplete,
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Complete", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
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

private enum class Action { ACCEPT, REJECT, MARK_READY, COMPLETE }

private fun actionsFor(status: String): List<Action> = when (status) {
    OrderStatus.PENDING -> listOf(Action.ACCEPT, Action.REJECT)
    OrderStatus.CONFIRMED -> listOf(Action.MARK_READY)
    OrderStatus.READY -> listOf(Action.COMPLETE)
    else -> emptyList()
}

private fun statusColor(status: String): Color = when (status) {
    OrderStatus.PENDING -> WarningOrange
    OrderStatus.CONFIRMED, OrderStatus.PREPARING -> AccentBlue
    OrderStatus.READY -> SuccessGreen
    OrderStatus.CANCELLED -> ErrorRed
    else -> TextHint
}
