package com.countertap.customer.ui.cart

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.countertap.customer.ui.theme.AccentBlue
import com.countertap.customer.ui.theme.BackgroundDark
import com.countertap.customer.ui.theme.CardBackground
import com.countertap.customer.ui.theme.CardElevated
import com.countertap.customer.ui.theme.DividerColor
import com.countertap.customer.ui.theme.ErrorRed
import com.countertap.customer.ui.theme.SuccessGreen
import com.countertap.customer.ui.theme.TextHint
import com.countertap.customer.ui.theme.TextPrimary
import com.countertap.customer.ui.theme.TextSecondary
import com.countertap.customer.viewmodel.CartViewModel
import com.countertap.customer.viewmodel.OrderState
import com.countertap.shared.CreditLineStatus
import com.countertap.shared.PaymentMethod

@Composable
fun CartScreen(
    tenantId: String,
    onBack: () -> Unit,
    onOrderPlaced: (orderId: String) -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val orderState by viewModel.orderState.collectAsState()
    val tableContext by viewModel.tableContext.collectAsState()
    val creditLine by viewModel.creditLine.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    var note by rememberSaveable { mutableStateOf("") }

    val creditAvailable = creditLine?.status == CreditLineStatus.ACTIVE
    val remainingCredit = ((creditLine?.limit ?: 0.0) - (creditLine?.balance ?: 0.0)).coerceAtLeast(0.0)
    val canUseCredit = creditAvailable && remainingCredit >= viewModel.totalAmount

    LaunchedEffect(orderState) {
        if (orderState is OrderState.Success) {
            onOrderPlaced((orderState as OrderState.Success).orderId)
            viewModel.resetOrderState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Column {
                Text("Your Cart", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                if (tableContext != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.TableRestaurant,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
                        Text(tableContext!!.tableName, fontSize = 12.sp, color = AccentBlue)
                    }
                } else if (items.isNotEmpty()) {
                    Text(
                        "${items.size} item${if (items.size > 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Cart items
        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            items(items) { cartItem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
                    ) {
                        Text(
                            cartItem.product.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        if (cartItem.selectedOptions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = cartItem.selectedOptions.joinToString(" · ") { it.optionName },
                                fontSize = 11.sp,
                                color = AccentBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "₹${cartItem.unitPrice.toInt()} each",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardElevated)
                    ) {
                        IconButton(
                            onClick = { viewModel.removeConfigured(cartItem.product, cartItem.selectedOptions) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove", tint = AccentBlue, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            cartItem.quantity.toString(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                        IconButton(
                            onClick = { viewModel.addConfigured(cartItem.product, cartItem.selectedOptions) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = AccentBlue, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "₹${(cartItem.unitPrice * cartItem.quantity).toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Special instructions (optional)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Summary + place order
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(CardBackground)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total amount", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "₹${viewModel.totalAmount.toInt()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )
                }
                Text(
                    "${items.sumOf { it.quantity }} items",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment method toggle — only shown when customer has an active credit line
            if (creditAvailable) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardElevated)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(PaymentMethod.CASH to "💵  Cash", PaymentMethod.CREDIT to "💳  Credit").forEach { (method, label) ->
                        val isSelected = paymentMethod == method
                        val enabled = method == PaymentMethod.CASH || canUseCredit
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isSelected && method == PaymentMethod.CREDIT -> SuccessGreen.copy(alpha = 0.15f)
                                        isSelected -> AccentBlue.copy(alpha = 0.15f)
                                        else -> androidx.compose.ui.graphics.Color.Transparent
                                    }
                                )
                                .clickable(enabled = enabled) { viewModel.setPaymentMethod(method) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        !enabled -> TextHint
                                        isSelected && method == PaymentMethod.CREDIT -> SuccessGreen
                                        isSelected -> AccentBlue
                                        else -> TextSecondary
                                    }
                                )
                                if (method == PaymentMethod.CREDIT) {
                                    Text(
                                        if (canUseCredit) "₹${remainingCredit.toInt()} available"
                                        else "Limit exceeded",
                                        fontSize = 10.sp,
                                        color = if (canUseCredit) SuccessGreen else TextHint
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (orderState is OrderState.Error) {
                Text(
                    (orderState as OrderState.Error).message,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (orderState is OrderState.Placing) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            } else {
                Button(
                    onClick = { viewModel.placeOrder(tenantId, note) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (paymentMethod == PaymentMethod.CREDIT) SuccessGreen else AccentBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = items.isNotEmpty()
                ) {
                    Text(
                        if (paymentMethod == PaymentMethod.CREDIT) "Place Order (Credit)" else "Place Order",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

