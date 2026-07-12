package com.countertap.customer.ui.menu

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.countertap.customer.ui.theme.SurfaceColor
import com.countertap.customer.ui.theme.TextPrimary
import com.countertap.customer.ui.theme.TextSecondary
import com.countertap.customer.ui.theme.WarningOrange
import com.countertap.customer.viewmodel.CartViewModel
import com.countertap.customer.viewmodel.MenuViewModel
import com.countertap.customer.viewmodel.TablePickerState
import com.countertap.customer.viewmodel.TablePickerViewModel
import com.countertap.shared.OptionGroup
import com.countertap.shared.Product
import com.countertap.shared.SelectedOption
import com.countertap.shared.Table
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    tenantId: String,
    onGoToCart: () -> Unit,
    onChangeRestaurant: () -> Unit = {},
    menuViewModel: MenuViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    tablePickerViewModel: TablePickerViewModel = hiltViewModel()
) {
    val uiState by menuViewModel.uiState.collectAsState()
    val cartItems by cartViewModel.items.collectAsState()
    val tableContext by cartViewModel.tableContext.collectAsState()
    val pickerState by tablePickerViewModel.state.collectAsState()
    val isShopOpen = uiState.shop?.active ?: true
    val cartCount = if (isShopOpen) cartItems.sumOf { it.quantity } else 0
    val cartTotal = cartItems.sumOf { it.unitPrice * it.quantity }

    var pickerProduct by remember { mutableStateOf<Product?>(null) }
    var showTableSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tableSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(tenantId) {
        menuViewModel.loadShop(tenantId)
        tablePickerViewModel.loadTables(tenantId)
    }

    if (showTableSheet) {
        val tables = (pickerState as? TablePickerState.Ready)?.tables ?: emptyList()
        TablePickerBottomSheet(
            sheetState = tableSheetState,
            tables = tables,
            currentContext = tableContext,
            onDismiss = {
                scope.launch { tableSheetState.hide() }.invokeOnCompletion { showTableSheet = false }
            },
            onPickTakeaway = {
                tablePickerViewModel.pickTakeaway()
                scope.launch { tableSheetState.hide() }.invokeOnCompletion { showTableSheet = false }
            },
            onPickTable = { table ->
                tablePickerViewModel.pickTable(table)
                scope.launch { tableSheetState.hide() }.invokeOnCompletion { showTableSheet = false }
            }
        )
    }

    if (pickerProduct != null) {
        OptionPickerSheet(
            product = pickerProduct!!,
            sheetState = sheetState,
            onDismiss = { pickerProduct = null },
            onAddToCart = { selectedOptions ->
                cartViewModel.addConfigured(pickerProduct!!, selectedOptions)
                scope.launch { sheetState.hide() }.invokeOnCompletion { pickerProduct = null }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.shop?.name ?: "Loading…",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (uiState.shop?.address?.isNotBlank() == true) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.shop!!.address,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    val hasTables = pickerState is TablePickerState.Ready &&
                        (pickerState as TablePickerState.Ready).tables.isNotEmpty()
                    if (hasTables || tableContext != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardElevated)
                                .clickable { showTableSheet = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.TableRestaurant,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = tableContext?.tableName?.ifBlank { "Takeaway" } ?: "Takeaway",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                IconButton(onClick = onChangeRestaurant) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "Change Restaurant",
                        tint = AccentBlue
                    )
                }
            }

            // Closed banner
            if (!isShopOpen && uiState.shop != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarningOrange.copy(alpha = 0.15f))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            "Shop is currently closed",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WarningOrange
                        )
                        Text(
                            "You can browse the menu but cannot place orders",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }

                uiState.error != null -> Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error!!, color = TextSecondary, fontSize = 14.sp)
                }

                else -> {
                    val grouped = uiState.products.groupBy { it.categoryId }
                    val knownCategoryIds = uiState.categories.map { it.id }.toSet()
                    val uncategorized = uiState.products.filter { it.categoryId !in knownCategoryIds }

                    if (uiState.products.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No items on the menu yet.", color = TextSecondary, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            uiState.categories.forEach { category ->
                                val products = grouped[category.id] ?: emptyList()
                                if (products.isNotEmpty()) {
                                    item {
                                        CategoryHeader(category.name)
                                    }
                                    items(products) { product ->
                                        ProductCard(
                                            product = product,
                                            quantity = cartViewModel.quantityOf(product.id),
                                            onAdd = { cartViewModel.add(product) },
                                            onRemove = { cartViewModel.remove(product) },
                                            onOpenPicker = { pickerProduct = product },
                                            isShopOpen = isShopOpen
                                        )
                                    }
                                }
                            }
                            if (uncategorized.isNotEmpty()) {
                                if (uiState.categories.isNotEmpty()) {
                                    item { CategoryHeader("OTHER") }
                                }
                                items(uncategorized) { product ->
                                    ProductCard(
                                        product = product,
                                        quantity = cartViewModel.quantityOf(product.id),
                                        onAdd = { cartViewModel.add(product) },
                                        onRemove = { cartViewModel.remove(product) },
                                        onOpenPicker = { pickerProduct = product },
                                        isShopOpen = isShopOpen
                                    )
                                }
                            }
                            item { Spacer(modifier = Modifier.height(96.dp)) }
                        }
                    }
                }
            }
        }

        // Cart bar
        if (cartCount > 0) {
            Button(
                onClick = onGoToCart,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View Cart  •  $cartCount item${if (cartCount > 1) "s" else ""}  •  ₹${cartTotal.toInt()}",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionPickerSheet(
    product: Product,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAddToCart: (List<SelectedOption>) -> Unit
) {
    // groupId -> set of selected optionIds
    val selections = remember { mutableStateMapOf<String, Set<String>>() }

    val allRequiredMet = product.optionGroups
        .filter { it.required }
        .all { group -> (selections[group.id]?.size ?: 0) > 0 }

    val selectedOptions: List<SelectedOption> = product.optionGroups.flatMap { group ->
        val selectedIds = selections[group.id] ?: emptySet()
        group.options
            .filter { it.id in selectedIds }
            .map { option -> SelectedOption(groupName = group.name, optionName = option.name, priceAddon = option.priceAddon) }
    }

    val extraPrice = selectedOptions.sumOf { it.priceAddon }
    val totalUnit = product.price + extraPrice

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            // Product title
            Text(product.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            if (product.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.description, fontSize = 13.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Base price: ₹${product.price.toInt()}",
                fontSize = 13.sp,
                color = AccentBlue,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = CardElevated)

            // Option groups
            product.optionGroups.forEach { group ->
                Spacer(modifier = Modifier.height(16.dp))
                OptionGroupSection(
                    group = group,
                    selectedIds = selections[group.id] ?: emptySet(),
                    onSelect = { optionId ->
                        if (group.multiSelect) {
                            val current = selections[group.id] ?: emptySet()
                            selections[group.id] = if (optionId in current) current - optionId else current + optionId
                        } else {
                            selections[group.id] = setOf(optionId)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Add to cart button
            Button(
                onClick = { onAddToCart(selectedOptions) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(12.dp),
                enabled = allRequiredMet
            ) {
                Text(
                    text = if (extraPrice > 0) "Add to Cart  •  ₹${totalUnit.toInt()}" else "Add to Cart  •  ₹${product.price.toInt()}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OptionGroupSection(
    group: OptionGroup,
    selectedIds: Set<String>,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(group.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (group.required) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(WarningOrange.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Required", fontSize = 10.sp, color = WarningOrange, fontWeight = FontWeight.Bold)
                    }
                }
                if (group.multiSelect) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Pick many", fontSize = 10.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        group.options.forEach { option ->
            val isSelected = option.id in selectedIds
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) AccentBlue.copy(alpha = 0.1f) else CardBackground)
                    .clickable { onSelect(option.id) }
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (group.multiSelect) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelect(option.id) },
                        colors = CheckboxDefaults.colors(checkedColor = AccentBlue)
                    )
                } else {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(option.id) },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                    )
                }
                Text(
                    text = option.name,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (option.priceAddon > 0) {
                    Text(
                        text = "+₹${option.priceAddon.toInt()}",
                        fontSize = 13.sp,
                        color = AccentBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CategoryHeader(name: String) {
    Row(
        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 10.dp, end = 20.dp),
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
            text = name.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentBlue,
            letterSpacing = 1.5.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TablePickerBottomSheet(
    sheetState: SheetState,
    tables: List<Table>,
    currentContext: com.countertap.customer.viewmodel.TableContext?,
    onDismiss: () -> Unit,
    onPickTakeaway: () -> Unit,
    onPickTable: (Table) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Text(
                "Choose your spot",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                "Orders will be tracked under your selection",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Takeaway option
            val isTakeaway = currentContext == null || currentContext.tableId.isEmpty()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isTakeaway) AccentBlue.copy(alpha = 0.12f) else CardBackground)
                    .clickable { onPickTakeaway() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = if (isTakeaway) AccentBlue else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("Takeaway", fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                        color = if (isTakeaway) AccentBlue else TextPrimary)
                    Text("Pick up at counter", fontSize = 12.sp, color = TextSecondary)
                }
                if (isTakeaway) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentBlue.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("Selected", fontSize = 11.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (tables.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CardElevated)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Tables",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                tables.forEach { table ->
                    val isSelected = currentContext?.tableId == table.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AccentBlue.copy(alpha = 0.12f) else CardBackground)
                            .clickable { onPickTable(table) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.TableRestaurant,
                            contentDescription = null,
                            tint = if (isSelected) AccentBlue else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                table.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) AccentBlue else TextPrimary
                            )
                            if (table.description.isNotBlank()) {
                                Text(
                                    table.description,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("Selected", fontSize = 11.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onOpenPicker: () -> Unit = {},
    isShopOpen: Boolean = true
) {
    val hasOptions = product.optionGroups.isNotEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
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
                .padding(start = 12.dp, top = 14.dp, bottom = 14.dp)
        ) {
            Text(
                text = product.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (product.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "₹${product.price.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )
                }
                if (hasOptions) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CardElevated)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Customisable",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (isShopOpen) {
            if (hasOptions) {
                if (quantity == 0) {
                    Button(
                        onClick = onOpenPicker,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("ADD", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentBlue.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = "$quantity in cart",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentBlue,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                        IconButton(onClick = onOpenPicker, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Add more", tint = AccentBlue, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                if (quantity == 0) {
                    Button(
                        onClick = onAdd,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("ADD", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardElevated)
                    ) {
                        IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove", tint = AccentBlue, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = quantity.toString(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                        IconButton(onClick = onAdd, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = AccentBlue, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
    }
}
