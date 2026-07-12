package com.countertap.business.ui.credit

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.countertap.business.ui.theme.AccentBlue
import com.countertap.business.ui.theme.BackgroundDark
import com.countertap.business.ui.theme.CardBackground
import com.countertap.business.ui.theme.DividerColor
import com.countertap.business.ui.theme.ErrorRed
import com.countertap.business.ui.theme.SuccessGreen
import com.countertap.business.ui.theme.TextHint
import com.countertap.business.ui.theme.TextPrimary
import com.countertap.business.ui.theme.TextSecondary
import com.countertap.business.ui.theme.WarningOrange
import com.countertap.business.viewmodel.CreditViewModel
import com.countertap.shared.CreditLine

@Composable
fun CreditScreen(
    onBack: () -> Unit,
    viewModel: CreditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var approvingCustomerId by remember { mutableStateOf<String?>(null) }
    var settlingLine by remember { mutableStateOf<CreditLine?>(null) }

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
            Text("Credit Lines", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }

            uiState.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp))
            }

            uiState.pendingLines.isEmpty() && uiState.activeLines.isEmpty() -> EmptyState()

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (uiState.pendingLines.isNotEmpty()) {
                    item { SectionHeader("PENDING REQUESTS", uiState.pendingLines.size, WarningOrange) }
                    items(uiState.pendingLines, key = { it.customerId }) { line ->
                        PendingCard(
                            line = line,
                            onApprove = { approvingCustomerId = line.customerId },
                            onReject = { viewModel.rejectCredit(line.customerId) }
                        )
                    }
                }

                if (uiState.activeLines.isNotEmpty()) {
                    item { SectionHeader("ACTIVE LINES", uiState.activeLines.size, SuccessGreen) }
                    items(uiState.activeLines, key = { it.customerId }) { line ->
                        ActiveCard(
                            line = line,
                            onOpenSettle = { settlingLine = line }
                        )
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    approvingCustomerId?.let { customerId ->
        ApproveLimitDialog(
            customerName = uiState.pendingLines.find { it.customerId == customerId }?.customerName ?: "",
            onConfirm = { limit ->
                viewModel.approveCredit(customerId, limit)
                approvingCustomerId = null
            },
            onDismiss = { approvingCustomerId = null }
        )
    }

    settlingLine?.let { line ->
        SettleDialog(
            line = line,
            onConfirm = { amount ->
                viewModel.markSettled(line.customerId, amount)
                settlingLine = null
            },
            onDismiss = { settlingLine = null }
        )
    }
}

@Composable
private fun SectionHeader(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(3.dp).height(14.dp)
                .clip(RoundedCornerShape(2.dp)).background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text("$label  •  $count", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, letterSpacing = 1.5.sp)
    }
}

@Composable
private fun PendingCard(line: CreditLine, onApprove: () -> Unit, onReject: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                    .background(WarningOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(line.customerName.ifBlank { "Unknown" }, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                if (line.customerEmail.isNotBlank()) {
                    Text(line.customerEmail, fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = DividerColor)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
            ) {
                Text("Reject", fontSize = 13.sp, color = ErrorRed, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onApprove,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Approve", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ActiveCard(line: CreditLine, onOpenSettle: () -> Unit) {
    val used = line.balance
    val limit = line.limit
    val remaining = (limit - used).coerceAtLeast(0.0)
    val usedFraction = if (limit > 0) (used / limit).toFloat().coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                    .background(SuccessGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(line.customerName.ifBlank { "Unknown" }, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                if (line.customerEmail.isNotBlank()) {
                    Text(line.customerEmail, fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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

        if (used > 0) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onOpenSettle,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text(
                    "Receive Payment  •  ₹${used.toInt()} owed",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
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
            Text("No credit line requests yet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text(
                "Customers can apply for credit from the menu screen. You'll see their requests here to approve or reject.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun SettleDialog(
    line: CreditLine,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf(line.balance.toInt().toString()) }
    val parsed = amountText.toDoubleOrNull()
    val isValid = parsed != null && parsed > 0 && parsed <= line.balance
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("Receive Payment", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "${line.customerName.ifBlank { "Customer" }} owes ₹${line.balance.toInt()}. Enter the amount they're paying now.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("Amount received (₹)", color = TextSecondary) },
                    placeholder = { Text("e.g. ${line.balance.toInt()}", color = TextHint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                if (parsed != null && parsed > line.balance) {
                    Spacer(Modifier.height(4.dp))
                    Text("Cannot exceed ₹${line.balance.toInt()} owed", fontSize = 11.sp, color = ErrorRed)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { parsed?.let { onConfirm(it) } },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                enabled = isValid
            ) { Text("Confirm", color = TextPrimary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun ApproveLimitDialog(
    customerName: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var limitText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("Set Credit Limit", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Set a credit limit for $customerName. They can place orders against this limit and pay you back later.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { c -> c.isDigit() } },
                    label = { Text("Credit limit (₹)", color = TextSecondary) },
                    placeholder = { Text("e.g. 500", color = TextHint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { limitText.toDoubleOrNull()?.let { onConfirm(it) } },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                enabled = limitText.toDoubleOrNull()?.let { it > 0 } == true
            ) { Text("Approve", color = TextPrimary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
