package com.countertap.customer.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
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
import com.countertap.customer.ui.home.CustomerBottomNav
import com.countertap.customer.ui.theme.AccentBlue
import com.countertap.customer.ui.theme.BackgroundDark
import com.countertap.customer.ui.theme.CardBackground
import com.countertap.customer.ui.theme.CardElevated
import com.countertap.customer.ui.theme.SurfaceColor
import com.countertap.customer.ui.theme.TextPrimary
import com.countertap.customer.ui.theme.TextSecondary
import com.countertap.customer.ui.theme.SuccessGreen
import com.countertap.customer.ui.theme.WarningOrange
import com.countertap.customer.viewmodel.CartViewModel
import com.countertap.customer.viewmodel.MenuDisplayViewModel
import com.countertap.customer.viewmodel.MenuViewModel
import com.countertap.customer.viewmodel.TablePickerState
import com.countertap.customer.viewmodel.TablePickerViewModel
import com.countertap.shared.CreditLineStatus
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
    onHome: () -> Unit,
    onOrders: () -> Unit,
    onScan: () -> Unit,
    onChangeRestaurant: () -> Unit = {},
    menuViewModel: MenuViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    tablePickerViewModel: TablePickerViewModel = hiltViewModel(),
    menuDisplayViewModel: MenuDisplayViewModel = hiltViewModel()
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
    val menuDisplayMode by menuDisplayViewModel.mode.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tableSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val creditLine by cartViewModel.creditLine.collectAsState()

    LaunchedEffect(tenantId) {
        menuViewModel.loadShop(tenantId)
        tablePickerViewModel.loadTables(tenantId)
        cartViewModel.loadCreditLine(tenantId)
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
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxSize()) {

            // Header
            val hasTables = pickerState is TablePickerState.Ready &&
                (pickerState as TablePickerState.Ready).tables.isNotEmpty()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .statusBarsPadding()
            ) {
                // Shop name + QR scanner button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 4.dp, top = 16.dp, bottom = 12.dp),
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
                    }
                    IconButton(onClick = onChangeRestaurant) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Change Restaurant",
                            tint = AccentBlue
                        )
                    }
                    IconButton(
                        onClick = {
                            menuDisplayViewModel.setMode(
                                if (menuDisplayMode == MenuDisplayMode.Scroll) {
                                    MenuDisplayMode.Book
                                } else {
                                    MenuDisplayMode.Scroll
                                }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = if (menuDisplayMode == MenuDisplayMode.Scroll) {
                                Icons.Default.AutoStories
                            } else {
                                Icons.AutoMirrored.Filled.ViewList
                            },
                            contentDescription = if (menuDisplayMode == MenuDisplayMode.Scroll) {
                                "Switch to book view"
                            } else {
                                "Switch to list view"
                            },
                            tint = AccentBlue
                        )
                    }
                }

                // Context chips row — table selector (left) and credit status (right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardElevated)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasTables || tableContext != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentBlue.copy(alpha = 0.22f))
                                .clickable { showTableSheet = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.TableRestaurant,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = tableContext?.tableName?.ifBlank { "Takeaway" } ?: "Takeaway",
                                fontSize = 13.sp,
                                color = AccentBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    when {
                        creditLine == null -> {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardBackground)
                                    .clickable {
                                        cartViewModel.requestCredit(tenantId, uiState.shop?.name ?: "")
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                Text("Apply for credit", fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                        creditLine!!.status == CreditLineStatus.PENDING -> {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(WarningOrange.copy(alpha = 0.22f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(14.dp))
                                Text("Credit: Pending", fontSize = 13.sp, color = WarningOrange, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        creditLine!!.status == CreditLineStatus.ACTIVE -> {
                            val remaining = (creditLine!!.limit - creditLine!!.balance).coerceAtLeast(0.0)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SuccessGreen.copy(alpha = 0.22f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                Text("₹${remaining.toInt()} credit", fontSize = 13.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
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
                    val sections = remember(uiState.categories, uiState.products) {
                        buildMenuSections(uiState.categories, uiState.products)
                    }

                    if (sections.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No items on the menu yet.", color = TextSecondary, fontSize = 14.sp)
                        }
                    } else {
                        val bottomPadding = if (cartCount > 0) 72.dp else 16.dp
                        when (menuDisplayMode) {
                            MenuDisplayMode.Scroll -> MenuScrollLayout(
                                sections = sections,
                                cartViewModel = cartViewModel,
                                isShopOpen = isShopOpen,
                                onOpenPicker = { pickerProduct = it },
                                bottomPadding = bottomPadding,
                                modifier = Modifier.weight(1f)
                            )
                            MenuDisplayMode.Book -> MenuBookLayout(
                                sections = sections,
                                cartViewModel = cartViewModel,
                                isShopOpen = isShopOpen,
                                onOpenPicker = { pickerProduct = it },
                                bottomPadding = bottomPadding,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
                }

                // Cart bar — above bottom nav
                if (cartCount > 0) {
                    Button(
                        onClick = onGoToCart,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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

            CustomerBottomNav(
                selectedIndex = -1,
                onHome = onHome,
                onOrders = onOrders,
                onScan = onScan
            )
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
