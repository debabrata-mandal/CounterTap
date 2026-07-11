package com.countertap.business.ui.menu

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.countertap.business.viewmodel.MenuViewModel
import com.countertap.shared.OptionGroup
import com.countertap.shared.Product
import com.countertap.shared.ProductOption
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    existingProduct: Product? = null,
    onDone: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by rememberSaveable { mutableStateOf(existingProduct?.name ?: "") }
    var description by rememberSaveable { mutableStateOf(existingProduct?.description ?: "") }
    var price by rememberSaveable { mutableStateOf(existingProduct?.price?.toString() ?: "") }
    var available by rememberSaveable { mutableStateOf(existingProduct?.available ?: true) }
    var selectedCategoryId by rememberSaveable { mutableStateOf(existingProduct?.categoryId ?: "") }
    var categoryExpanded by rememberSaveable { mutableStateOf(false) }

    val optionGroups = remember {
        mutableStateListOf<OptionGroup>().also { list ->
            existingProduct?.optionGroups?.let { list.addAll(it) }
        }
    }

    val prevSaving = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(uiState.isSaving) {
        if (prevSaving.value && !uiState.isSaving && uiState.error == null) {
            onDone()
        }
        prevSaving.value = uiState.isSaving
    }

    val selectedCategoryName = uiState.categories.find { it.id == selectedCategoryId }?.name ?: "None"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingProduct == null) "Add product" else "Edit product") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Base price (₹)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = { selectedCategoryId = ""; categoryExpanded = false }
                    )
                    uiState.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = { selectedCategoryId = category.id; categoryExpanded = false }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Available", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = available, onCheckedChange = { available = it })
            }

            // ── Option Groups ────────────────────────────────────────────────
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Options / Variants", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(
                        "e.g. Size, Sugar level, Add-ons",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = {
                        optionGroups.add(
                            OptionGroup(
                                id = UUID.randomUUID().toString(),
                                name = "",
                                required = false,
                                multiSelect = false,
                                options = emptyList()
                            )
                        )
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" Add group")
                }
            }

            optionGroups.forEachIndexed { groupIdx, group ->
                OptionGroupCard(
                    group = group,
                    onUpdate = { updated -> optionGroups[groupIdx] = updated },
                    onRemove = { optionGroups.removeAt(groupIdx) }
                )
            }

            HorizontalDivider()
            // ────────────────────────────────────────────────────────────────

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        val priceVal = price.toDoubleOrNull() ?: 0.0
                        val product = (existingProduct ?: Product()).copy(
                            name = name.trim(),
                            description = description.trim(),
                            price = priceVal,
                            categoryId = selectedCategoryId,
                            available = available,
                            optionGroups = optionGroups.toList()
                        )
                        viewModel.saveProduct(product)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && price.isNotBlank()
                ) {
                    Text(if (existingProduct == null) "Add product" else "Save changes")
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OptionGroupCard(
    group: OptionGroup,
    onUpdate: (OptionGroup) -> Unit,
    onRemove: () -> Unit
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Group header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = group.name,
                onValueChange = { onUpdate(group.copy(name = it)) },
                label = { Text("Group name", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remove group", tint = MaterialTheme.colorScheme.error)
            }
        }

        // Toggles
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Required", fontSize = 14.sp)
                Switch(
                    checked = group.required,
                    onCheckedChange = { onUpdate(group.copy(required = it)) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Multi-select", fontSize = 14.sp)
                Switch(
                    checked = group.multiSelect,
                    onCheckedChange = { onUpdate(group.copy(multiSelect = it)) }
                )
            }
        }

        // Options list
        group.options.forEachIndexed { optIdx, option ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = option.name,
                    onValueChange = { newName ->
                        val updatedOptions = group.options.toMutableList()
                        updatedOptions[optIdx] = option.copy(name = newName)
                        onUpdate(group.copy(options = updatedOptions))
                    },
                    label = { Text("Option", fontSize = 12.sp) },
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = if (option.priceAddon == 0.0) "" else option.priceAddon.toString(),
                    onValueChange = { newAddon ->
                        val updatedOptions = group.options.toMutableList()
                        updatedOptions[optIdx] = option.copy(priceAddon = newAddon.toDoubleOrNull() ?: 0.0)
                        onUpdate(group.copy(options = updatedOptions))
                    },
                    label = { Text("+₹", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                IconButton(
                    onClick = {
                        val updatedOptions = group.options.toMutableList()
                        updatedOptions.removeAt(optIdx)
                        onUpdate(group.copy(options = updatedOptions))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remove option", modifier = Modifier.size(18.dp))
                }
            }
        }

        // Add option button
        TextButton(
            onClick = {
                val newOption = ProductOption(id = UUID.randomUUID().toString(), name = "", priceAddon = 0.0)
                onUpdate(group.copy(options = group.options + newOption))
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(" Add option", fontSize = 13.sp)
        }
    }
}
