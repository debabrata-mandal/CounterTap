package com.countertap.business.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.countertap.business.viewmodel.SaveState
import com.countertap.business.viewmodel.TenantState
import com.countertap.business.viewmodel.TenantViewModel

@Composable
fun UpiSetupScreen(
    onSetupComplete: () -> Unit,
    isEditMode: Boolean = false,
    onBack: () -> Unit = {},
    viewModel: TenantViewModel = hiltViewModel()
) {
    val saveState by viewModel.saveState.collectAsState()
    val tenantState by viewModel.tenantState.collectAsState()

    val existingShop = (tenantState as? TenantState.Ready)?.shop

    var upiId by rememberSaveable { mutableStateOf(if (isEditMode) existingShop?.upiId ?: "" else "") }

    LaunchedEffect(existingShop) {
        if (isEditMode && existingShop != null && upiId.isEmpty()) {
            upiId = existingShop.upiId
        }
    }

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            viewModel.resetSaveState()
            if (isEditMode) onBack() else onSetupComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = if (isEditMode) Arrangement.Top else Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isEditMode) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = if (isEditMode) "Edit UPI ID" else "Set your UPI ID",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Customers will pay to this UPI ID",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = upiId,
            onValueChange = { upiId = it },
            label = { Text("UPI ID") },
            placeholder = { Text("e.g. yourname@paytm") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("Accepts payments via GPay, PhonePe, Paytm, and all UPI apps") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (saveState is SaveState.Saving) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        } else {
            Button(
                onClick = { viewModel.saveUpiId(upiId.trim()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = upiId.isNotBlank()
            ) {
                Text(if (isEditMode) "Save" else "Finish setup")
            }

            if (saveState is SaveState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (saveState as SaveState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
