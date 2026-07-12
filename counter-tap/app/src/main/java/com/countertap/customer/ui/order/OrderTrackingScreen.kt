package com.countertap.customer.ui.order

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.countertap.customer.viewmodel.OrderTrackingViewModel
import com.countertap.shared.Order
import com.countertap.shared.OrderStatus

private data class StatusStep(
    val status: String,
    val label: String,
    val description: String
)

private val STEPS = listOf(
    StatusStep(OrderStatus.PENDING, "Order Placed", "Waiting for the shop to accept"),
    StatusStep(OrderStatus.CONFIRMED, "Confirmed", "Your order is being prepared"),
    StatusStep(OrderStatus.READY, "Ready", "Come collect your order!"),
    StatusStep(OrderStatus.COMPLETED, "Collected", "Enjoy your order!")
)

private val STATUS_INDEX = mapOf(
    OrderStatus.PENDING to 0,
    OrderStatus.CONFIRMED to 1,
    OrderStatus.PREPARING to 1,
    OrderStatus.READY to 2,
    OrderStatus.COMPLETED to 3
)

@Composable
fun OrderTrackingScreen(
    tenantId: String,
    orderId: String,
    viewModel: OrderTrackingViewModel = hiltViewModel()
) {
    LaunchedEffect(tenantId, orderId) {
        viewModel.startTracking(tenantId, orderId)
    }

    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Text(
            "Order Status",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        when {
            state.isLoading -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlue)
            }

            state.error != null -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(state.error!!, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
            }

            state.order == null -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Order not found", color = TextSecondary, fontSize = 14.sp)
            }

            else -> {
                val order = state.order!!
                val isCancelled = order.status == OrderStatus.CANCELLED

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // Order ID
                    Text(
                        "#${orderId.take(6).uppercase()}",
                        fontSize = 13.sp,
                        color = TextHint,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    if (isCancelled) {
                        CancelledCard()
                    } else {
                        StatusTimeline(currentStatus = order.status)
                        Spacer(modifier = Modifier.height(12.dp))
                        if (order.paymentStatus == "paid") {
                            PaymentConfirmedBanner()
                        } else if (order.tableName.isNotBlank()) {
                            TableTabReminder(tableName = order.tableName)
                        } else {
                            CashPaymentReminder(amount = order.totalAmount.toInt())
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    OrderSummaryCard(order)
                    Spacer(modifier = Modifier.height(24.dp))
                }

            }
        }
    }
}

@Composable
private fun StatusTimeline(currentStatus: String) {
    val currentIndex = STATUS_INDEX[currentStatus] ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(20.dp)
    ) {
        STEPS.forEachIndexed { index, step ->
            val isDone = index < currentIndex
            val isCurrent = index == currentIndex
            val isPending = index > currentIndex

            Row(verticalAlignment = Alignment.Top) {
                // Icon column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDone -> SuccessGreen.copy(alpha = 0.15f)
                                    isCurrent -> AccentBlue.copy(alpha = 0.15f)
                                    else -> CardBackground
                                }
                            )
                    ) {
                        when {
                            isDone -> Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            isCurrent -> Icon(
                                Icons.Default.Circle,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            else -> Icon(
                                Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = TextHint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (index < STEPS.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(36.dp)
                                .background(if (isDone) SuccessGreen.copy(alpha = 0.4f) else DividerColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Text column
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        step.label,
                        fontSize = 14.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isDone -> SuccessGreen
                            isCurrent -> TextPrimary
                            else -> TextHint
                        }
                    )
                    if (isCurrent) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(step.description, fontSize = 12.sp, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(if (index < STEPS.lastIndex) 20.dp else 0.dp))
                }
            }
        }
    }
}

@Composable
private fun PaymentConfirmedBanner() {
    Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SuccessGreen.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("✅", fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                "Payment confirmed",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SuccessGreen
            )
            Text(
                "The shop has received your payment",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun CashPaymentReminder(amount: Int) {
    Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WarningOrange.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("💵", fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                "Pay ₹$amount cash at the counter",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarningOrange
            )
            Text(
                "Please keep exact change ready",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun TableTabReminder(tableName: String) {
    Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AccentBlue.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.TableRestaurant, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                "Added to $tableName's tab",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentBlue
            )
            Text(
                "Pay when you're done — settle with the shop at the end",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun CancelledCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ErrorRed.copy(alpha = 0.1f))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(ErrorRed.copy(alpha = 0.15f))
        ) {
            Text("✕", fontSize = 24.sp, color = ErrorRed)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Order Cancelled", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "The shop has cancelled your order.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OrderSummaryCard(order: Order) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(16.dp)
    ) {
        Text("Your Order", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(12.dp))

        order.items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${item.quantity}×",
                    fontSize = 13.sp,
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(28.dp)
                )
                Text(item.productName, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                Text("₹${(item.price * item.quantity).toInt()}", fontSize = 14.sp, color = TextSecondary)
            }
        }

        if (order.note.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Note: ${order.note}", fontSize = 12.sp, color = TextHint)
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = DividerColor)
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Total", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.weight(1f))
            Text("₹${order.totalAmount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
        }
    }
}
