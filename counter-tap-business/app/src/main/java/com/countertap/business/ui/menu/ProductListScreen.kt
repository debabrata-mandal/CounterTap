package com.countertap.business.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import kotlin.math.abs
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.countertap.business.ui.theme.AccentBlue
import com.countertap.business.ui.theme.BackgroundDark
import com.countertap.business.ui.theme.CardBackground
import com.countertap.business.ui.theme.ErrorRed
import com.countertap.business.ui.theme.SuccessGreen
import com.countertap.business.ui.theme.TextHint
import com.countertap.business.ui.theme.TextPrimary
import com.countertap.business.ui.theme.TextSecondary
import com.countertap.business.viewmodel.MenuViewModel
import com.countertap.shared.Product
import kotlinx.coroutines.launch

@Composable
fun ProductListScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onManageCategories: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = BackgroundDark,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Menu",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (uiState.products.isNotEmpty()) {
                            val catCount = uiState.categories.size
                            Text(
                                text = "${uiState.products.size} items  •  $catCount ${if (catCount == 1) "category" else "categories"}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardBackground)
                            .clickable(onClick = onManageCategories)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            "Categories",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AccentBlue
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = AccentBlue,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Add Item", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundDark)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AccentBlue
                    )
                }

                uiState.products.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(AccentBlue.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Text("No items yet", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Tap 'Add Item' to build your menu", fontSize = 13.sp, color = TextSecondary)
                    }
                }

                else -> {
                    val knownCategoryIds = remember(uiState.categories) {
                        uiState.categories.map { it.id }.toSet()
                    }
                    val grouped = remember(uiState.products) {
                        uiState.products.groupBy { it.categoryId }
                    }
                    val categoriesWithProducts = remember(uiState.categories, grouped) {
                        uiState.categories.filter { (grouped[it.id] ?: emptyList()).isNotEmpty() }
                    }
                    val uncategorized = remember(uiState.products, knownCategoryIds) {
                        uiState.products.filter { it.categoryId !in knownCategoryIds }
                    }

                    // Map: categoryId -> first index of its header in the LazyColumn
                    val indexMap = remember(categoriesWithProducts, grouped, uncategorized) {
                        buildMap {
                            var idx = 0
                            categoriesWithProducts.forEach { category ->
                                put(category.id, idx)
                                idx += 1 + (grouped[category.id]?.size ?: 0)
                            }
                            if (uncategorized.isNotEmpty()) put("__other__", idx)
                        }
                    }

                    // Auto-highlight the tab matching the first visible section
                    val activeTabId by remember(indexMap) {
                        derivedStateOf {
                            val firstVisible = listState.firstVisibleItemIndex
                            indexMap.entries
                                .sortedBy { it.value }
                                .lastOrNull { it.value <= firstVisible }
                                ?.key
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {

                        // Category tab row — only when there are multiple sections
                        if (categoriesWithProducts.size > 1 || uncategorized.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BackgroundDark)
                                    .padding(vertical = 10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categoriesWithProducts) { category ->
                                    val count = grouped[category.id]?.size ?: 0
                                    CategoryTab(
                                        name = category.name,
                                        count = count,
                                        isActive = activeTabId == category.id,
                                        onClick = {
                                            scope.launch {
                                                listState.animateScrollToItem(
                                                    indexMap[category.id] ?: 0
                                                )
                                            }
                                        }
                                    )
                                }
                                if (uncategorized.isNotEmpty()) {
                                    item {
                                        CategoryTab(
                                            name = "Other",
                                            count = uncategorized.size,
                                            isActive = activeTabId == "__other__",
                                            onClick = {
                                                scope.launch {
                                                    listState.animateScrollToItem(
                                                        indexMap["__other__"] ?: 0
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = AccentBlue.copy(alpha = 0.10f))
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            categoriesWithProducts.forEach { category ->
                                val products = grouped[category.id] ?: emptyList()
                                item(key = "header_${category.id}") {
                                    SectionHeader(
                                        name = category.name,
                                        count = products.size
                                    )
                                }
                                items(products, key = { it.id }) { product ->
                                    ProductItem(
                                        product = product,
                                        onEdit = { onEditProduct(product) },
                                        onDelete = { viewModel.deleteProduct(product.id) },
                                        onToggle = { viewModel.toggleAvailability(product) }
                                    )
                                }
                            }

                            if (uncategorized.isNotEmpty()) {
                                item(key = "header_other") {
                                    SectionHeader(
                                        name = "Other",
                                        count = uncategorized.size,
                                        color = TextHint
                                    )
                                }
                                items(uncategorized, key = { it.id }) { product ->
                                    ProductItem(
                                        product = product,
                                        onEdit = { onEditProduct(product) },
                                        onDelete = { viewModel.deleteProduct(product.id) },
                                        onToggle = { viewModel.toggleAvailability(product) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTab(
    name: String,
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) AccentBlue else CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) TextPrimary else TextSecondary
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    if (isActive) TextPrimary.copy(alpha = 0.20f) else AccentBlue.copy(alpha = 0.15f)
                )
                .padding(horizontal = 6.dp, vertical = 1.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) TextPrimary else AccentBlue
            )
        }
    }
}

@Composable
private fun SectionHeader(name: String, count: Int, color: Color = AccentBlue) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = name.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                letterSpacing = 1.5.sp
            )
        }
        Text(
            text = "$count item${if (count != 1) "s" else ""}",
            fontSize = 11.sp,
            color = TextHint
        )
    }
}

private val avatarPalette = listOf(
    Color(0xFF5C6BC0), Color(0xFF26A69A), Color(0xFFEF5350),
    Color(0xFFFF7043), Color(0xFF66BB6A), Color(0xFFAB47BC),
    Color(0xFF26C6DA), Color(0xFF8D6E63), Color(0xFF78909C), Color(0xFFF4511E)
)

private fun avatarColor(name: String): Color =
    avatarPalette[abs(name.hashCode()) % avatarPalette.size]

@Composable
private fun ProductItem(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    val dim = if (product.available) 1f else 0.45f
    val accentColor = if (product.available) AccentBlue else TextHint

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
                .background(accentColor.copy(alpha = 0.8f))
        )

        // Product image / avatar
        Box(
            modifier = Modifier
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
                .size(54.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(avatarColor(product.name).copy(alpha = if (product.available) 1f else 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            if (product.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = product.name.first().uppercaseChar().toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = if (product.available) 0.95f else 0.5f)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, top = 12.dp, bottom = 10.dp, end = 4.dp)
        ) {
            // Name + price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary.copy(alpha = dim),
                    textDecoration = if (!product.available) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "₹${product.price.toInt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            if (product.description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = TextSecondary.copy(alpha = dim),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))

            // Bottom row: availability badge + edit + delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (product.available) SuccessGreen.copy(alpha = 0.14f)
                            else TextHint.copy(alpha = 0.14f)
                        )
                        .clickable(onClick = onToggle)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (product.available) SuccessGreen else TextHint)
                    )
                    Text(
                        text = if (product.available) "Available" else "Unavailable",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (product.available) SuccessGreen else TextHint
                    )
                }

                Spacer(Modifier.weight(1f))

                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = AccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
