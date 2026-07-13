package com.countertap.customer.ui.credit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.countertap.customer.viewmodel.CustomerCreditViewModel
import com.countertap.shared.CreditLine
import com.countertap.shared.CreditLineStatus

@Composable
fun CustomerCreditLinesScreen(
    onBack: () -> Unit,
    viewModel: CustomerCreditViewModel = hiltViewModel()
) {
    val creditLines by viewModel.creditLines.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var requestingIncreaseFor by remember { mutableStateOf<CreditLine?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AccentBlue)
            }
            Text("My Credit Lines", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }

            creditLines.isEmpty() -> EmptyState()

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(creditLines, key = { "${it.tenantId}_${it.customerId}" }) { line ->
                    CreditLineCard(
                        line = line,
                        onRequestIncrease = { requestingIncreaseFor = line }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    requestingIncreaseFor?.let { line ->
        RequestIncreaseDialog(
            line = line,
            onConfirm = { amount ->
                viewModel.requestLimitIncrease(line.tenantId, amount)
                requestingIncreaseFor = null
            },
            onDismiss = { requestingIncreaseFor = null }
        )
    }
}

@Composable
private fun CreditLineCard(line: CreditLine, onRequestIncrease: () -> Unit) {
    val isActive = line.status == CreditLineStatus.ACTIVE
    val isPending = line.status == CreditLineStatus.PENDING
    val isRejected = line.status == CreditLineStatus.REJECTED
    val statusColor = when {
        isActive -> SuccessGreen
        isPending -> WarningOrange
        isRejected -> ErrorRed
        else -> TextHint
    }
    val statusLabel = when {
        isActive -> "Active"
        isPending -> "Pending approval"
        isRejected -> "Rejected"
        else -> line.status
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                    .background(statusColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = statusColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(line.shopName.ifBlank { "Shop" }, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(statusLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }
        }

        if (isActive) {
            val used = line.balance
            val limit = line.limit
            val remaining = (limit - used).coerceAtLeast(0.0)
            val usedFraction = if (limit > 0) (used / limit).toFloat().coerceIn(0f, 1f) else 0f
            val hasPendingIncrease = line.pendingLimitIncrease > 0

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "₹${used.toInt()} owed",
                    fontSize = 13.sp,
                    color = if (used > 0) WarningOrange else TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Limit ₹${limit.toInt()}", fontSize = 13.sp, color = TextHint)
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(6.dp)
                    .clip(RoundedCornerShape(3.dp)).background(DividerColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(usedFraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (usedFraction > 0.8f) ErrorRed else WarningOrange)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text("₹${remaining.toInt()} remaining", fontSize = 11.sp, color = TextHint)

            Spacer(Modifier.height(12.dp))

            if (hasPendingIncrease) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(WarningOrange.copy(alpha = 0.10f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Increase request of +₹${line.pendingLimitIncrease.toInt()} pending approval",
                        fontSize = 12.sp,
                        color = WarningOrange,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onRequestIncrease,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue)
                ) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Request Limit Increase", fontSize = 13.sp, color = AccentBlue, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (isPending) {
            Spacer(Modifier.height(10.dp))
            Text(
                "The shop owner will review your request and set a credit limit.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )
        }

        if (isRejected) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Your credit request was not approved. You can contact the shop directly.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = TextHint, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("No credit lines yet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text(
                "You can apply for credit at any shop from the menu screen. Approved credit lets you place orders and pay later.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun RequestIncreaseDialog(
    line: CreditLine,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val parsed = amountText.toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("Request Limit Increase", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Current limit at ${line.shopName.ifBlank { "this shop" }}: ₹${line.limit.toInt()}. Enter how much additional credit you'd like to request.",
                    fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("Additional amount (₹)", color = TextSecondary) },
                    placeholder = { Text("e.g. 200", color = TextHint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue, unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { parsed?.let { onConfirm(it) } },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                enabled = parsed != null && parsed > 0
            ) { Text("Request", color = TextPrimary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}
