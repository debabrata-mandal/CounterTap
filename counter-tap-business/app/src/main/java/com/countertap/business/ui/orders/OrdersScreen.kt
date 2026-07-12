package com.countertap.business.ui.orders

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.countertap.business.viewmodel.OrdersUiState
import com.countertap.business.viewmodel.OrdersViewModel
import com.countertap.shared.Order
import com.countertap.shared.OrderStatus
import com.countertap.shared.TableSession
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrdersScreen(viewModel: OrdersViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val hasTables = uiState.openSessions.isNotEmpty() ||
            uiState.activeOrders.any { it.tableSessionId.isNotEmpty() }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = if (hasTables) 4.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Orders", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        // Sub-tabs — only shown when table orders exist
        if (hasTables) {
            OrdersSubTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        }

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = AccentBlue) }

            uiState.error != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text(uiState.error!!, color = TextSecondary, fontSize = 14.sp) }

            else -> if (hasTables && selectedTab == 1) {
                TableSessionsTab(uiState, viewModel)
            } else {
                IndividualOrdersTab(uiState, viewModel)
            }
        }
    }
}

@Composable
private fun OrdersSubTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CardBackground)
            .padding(4.dp)
    ) {
        listOf("Individual", "Tables").forEachIndexed { index, label ->
            val isSelected = index == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) AccentBlue else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun IndividualOrdersTab(uiState: OrdersUiState, viewModel: OrdersViewModel) {
    val activeOrders = uiState.activeOrders.filter { it.tableSessionId.isEmpty() }
    val doneOrders = uiState.doneOrders.filter { it.tableSessionId.isEmpty() }
    // Fall back to all orders if no table sessions exist (shops without tables)
    val showActive = if (uiState.openSessions.isEmpty() && uiState.activeOrders.none { it.tableSessionId.isNotEmpty() })
        uiState.activeOrders else activeOrders
    val showDone = if (uiState.openSessions.isEmpty() && uiState.activeOrders.none { it.tableSessionId.isNotEmpty() })
        uiState.doneOrders else doneOrders

    if (showActive.isEmpty() && showDone.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No orders yet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                Text("New orders will appear here in real time", fontSize = 14.sp, color = TextSecondary)
            }
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (showActive.isNotEmpty()) {
            item { SectionHeader("ACTIVE", showActive.size) }
            items(showActive, key = { it.id }) { order ->
                OrderCard(
                    order = order,
                    onAccept = { viewModel.updateStatus(order.id, order.customerId, OrderStatus.CONFIRMED) },
                    onReject = { viewModel.updateStatus(order.id, order.customerId, OrderStatus.CANCELLED) },
                    onReady = { viewModel.updateStatus(order.id, order.customerId, OrderStatus.READY) },
                    onComplete = { viewModel.updateStatus(order.id, order.customerId, OrderStatus.COMPLETED) },
                    onMarkPaid = { viewModel.markAsPaid(order.id) }
                )
            }
        }
        if (showDone.isNotEmpty()) {
            item { SectionHeader("COMPLETED / CANCELLED", showDone.size) }
            items(showDone, key = { it.id }) { order ->
                OrderCard(order = order, onAccept = {}, onReject = {}, onReady = {}, onComplete = {}, onMarkPaid = {})
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun TableSessionsTab(uiState: OrdersUiState, viewModel: OrdersViewModel) {
    if (uiState.openSessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.TableRestaurant, contentDescription = null, tint = TextHint, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(12.dp))
                Text("No active table sessions", fontSize = 16.sp, color = TextSecondary)
            }
        }
        return
    }

    // All orders (active + done) so session cards show the full running bill
    val allOrders = uiState.activeOrders + uiState.doneOrders

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { SectionHeader("ACTIVE SESSIONS", uiState.openSessions.size) }
        items(uiState.openSessions, key = { it.id }) { session ->
            val sessionOrders = allOrders
                .filter { it.tableSessionId == session.id }
                .sortedBy { it.createdAt }
            TableSessionCard(
                session = session,
                orders = sessionOrders,
                onSettle = { viewModel.settleAndCloseSession(session.id) },
                onAccept = { order -> viewModel.updateStatus(order.id, order.customerId, OrderStatus.CONFIRMED) },
                onReject = { order -> viewModel.updateStatus(order.id, order.customerId, OrderStatus.CANCELLED) },
                onReady  = { order -> viewModel.updateStatus(order.id, order.customerId, OrderStatus.READY) }
            )
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun TableSessionCard(
    session: TableSession,
    orders: List<Order>,
    onSettle: () -> Unit,
    onAccept: (Order) -> Unit,
    onReject: (Order) -> Unit,
    onReady: (Order) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
    ) {
        // Session header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AccentBlue.copy(alpha = 0.1f))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TableRestaurant, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(session.tableName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        "${orders.size} order${if (orders.size != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            Text("₹${session.totalAmount.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
        }

        // Orders in session — kitchen-only actions, no per-order payment
        if (orders.isEmpty()) {
            HorizontalDivider(color = DividerColor)
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No orders yet", fontSize = 13.sp, color = TextHint)
            }
        } else {
            orders.forEach { order ->
                HorizontalDivider(color = DividerColor)
                TableOrderCard(
                    order = order,
                    onAccept = { onAccept(order) },
                    onReject = { onReject(order) },
                    onReady  = { onReady(order) }
                )
            }
        }

        // Settle & Close — pays all orders and closes session atomically
        HorizontalDivider(color = DividerColor)
        Button(
            onClick = onSettle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .height(44.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
        ) {
            Text(
                "Settle & Close  •  ₹${session.totalAmount.toInt()}",
                fontSize = 14.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TableOrderCard(
    order: Order,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onReady: () -> Unit
) {
    val accentColor = statusColor(order.status)
    val timeStr = order.createdAt?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
    } ?: ""

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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(order.status)
                if (order.customerName.isNotBlank()) {
                    Text(order.customerName, fontSize = 12.sp, color = TextSecondary)
                }
            }
            Text(timeStr, fontSize = 12.sp, color = TextHint)
        }

        Spacer(Modifier.height(8.dp))
        order.items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${item.quantity}×  ${item.productName}",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text("₹${(item.price * item.quantity).toInt()}", fontSize = 13.sp, color = TextSecondary)
            }
            if (item.selectedOptions.isNotEmpty()) {
                Text(
                    item.selectedOptions.joinToString(" · ") { it.optionName },
                    fontSize = 11.sp,
                    color = AccentBlue,
                    modifier = Modifier.padding(start = 22.dp, top = 1.dp)
                )
            }
        }

        if (order.note.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text("Note: ${order.note}", fontSize = 11.sp, color = TextHint)
        }

        // Kitchen actions only — no Complete, no Mark Paid for table orders
        val actions = actionsFor(order.status)
        if (actions.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                actions.forEach { action ->
                    when (action) {
                        Action.ACCEPT -> Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f).height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Accept", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold) }

                        Action.REJECT -> OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f).height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                        ) { Text("Reject", fontSize = 12.sp, color = ErrorRed, fontWeight = FontWeight.Bold) }

                        Action.MARK_READY -> Button(
                            onClick = onReady,
                            modifier = Modifier.fillMaxWidth().height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Mark Ready", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold) }

                        Action.COMPLETE -> { /* handled at session level via Settle & Close */ }
                    }
                }
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
    onComplete: () -> Unit,
    onMarkPaid: () -> Unit
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                    if (item.selectedOptions.isNotEmpty()) {
                        Text(
                            text = item.selectedOptions.joinToString(" · ") { it.optionName },
                            fontSize = 11.sp,
                            color = AccentBlue,
                            modifier = Modifier.padding(start = 22.dp, top = 1.dp, bottom = 2.dp)
                        )
                    }
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total", fontSize = 14.sp, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "₹${order.totalAmount.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )
                    val isPaid = order.paymentStatus == "paid"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background((if (isPaid) SuccessGreen else WarningOrange).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (isPaid) "Paid" else "Unpaid",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaid) SuccessGreen else WarningOrange
                        )
                    }
                }
            }

            if (order.paymentStatus != "paid" && order.status !in setOf(OrderStatus.COMPLETED, OrderStatus.CANCELLED)) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onMarkPaid,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Mark as Paid", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
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
                                shape = RoundedCornerShape(8.dp),
                                enabled = order.paymentStatus == "paid"
                            ) {
                                Text(
                                    if (order.paymentStatus == "paid") "Complete" else "Complete (collect payment first)",
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
