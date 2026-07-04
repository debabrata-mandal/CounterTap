package com.countertap.customer.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.countertap.customer.ui.theme.TextPrimary
import com.countertap.customer.ui.theme.TextSecondary
import com.countertap.customer.viewmodel.CartViewModel
import com.countertap.customer.viewmodel.MenuViewModel
import com.countertap.shared.Product

@Composable
fun MenuScreen(
    tenantId: String,
    onGoToCart: () -> Unit,
    menuViewModel: MenuViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val uiState by menuViewModel.uiState.collectAsState()
    val cartItems by cartViewModel.items.collectAsState()
    val cartCount = cartViewModel.totalCount

    LaunchedEffect(tenantId) { menuViewModel.loadShop(tenantId) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = uiState.shop?.name ?: "Loading…",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (uiState.shop?.address?.isNotBlank() == true) {
                    Text(
                        text = uiState.shop!!.address,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
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
                                        Text(
                                            text = category.name.uppercase(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentBlue,
                                            letterSpacing = 1.2.sp,
                                            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
                                        )
                                    }
                                    items(products) { product ->
                                        ProductCard(
                                            product = product,
                                            quantity = cartViewModel.quantityOf(product.id),
                                            onAdd = { cartViewModel.add(product) },
                                            onRemove = { cartViewModel.remove(product) }
                                        )
                                    }
                                }
                            }
                            if (uncategorized.isNotEmpty()) {
                                if (uiState.categories.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "OTHER",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentBlue,
                                            letterSpacing = 1.2.sp,
                                            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
                                        )
                                    }
                                }
                                items(uncategorized) { product ->
                                    ProductCard(
                                        product = product,
                                        quantity = cartViewModel.quantityOf(product.id),
                                        onAdd = { cartViewModel.add(product) },
                                        onRemove = { cartViewModel.remove(product) }
                                    )
                                }
                            }
                            item { Spacer(modifier = Modifier.height(88.dp)) }
                        }
                    }
                }
            }
        }

        // Cart FAB
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
                    text = "View Cart  •  $cartCount item${if (cartCount > 1) "s" else ""}  •  ₹${cartViewModel.totalAmount.toInt()}",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = product.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "₹${product.price.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
            if (product.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = product.description, fontSize = 12.sp, color = TextSecondary, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Remove", tint = AccentBlue)
                }
                Text(
                    text = quantity.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = AccentBlue)
                }
            }
        }
    }
}
