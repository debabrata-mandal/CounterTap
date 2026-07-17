package com.countertap.customer.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.countertap.customer.ui.theme.AccentBlue
import com.countertap.customer.ui.theme.BackgroundDark
import com.countertap.customer.ui.theme.CardBackground
import com.countertap.customer.ui.theme.TextPrimary
import com.countertap.customer.ui.theme.TextSecondary
import com.countertap.customer.viewmodel.CartViewModel
import com.countertap.shared.Product
import kotlinx.coroutines.launch

@Composable
fun MenuScrollLayout(
    sections: List<MenuSection>,
    cartViewModel: CartViewModel,
    isShopOpen: Boolean,
    onOpenPicker: (Product) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 96.dp,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val indexMap = remember(sections) {
        buildMap {
            var idx = 0
            sections.forEach { section ->
                put(section.id, idx)
                idx += 1 + section.products.size
            }
        }
    }

    val activeTabId by remember(indexMap) {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            indexMap.entries
                .sortedBy { it.value }
                .lastOrNull { it.value <= firstVisible }
                ?.key
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (sections.size > 1) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark)
                    .padding(vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sections, key = { it.id }) { section ->
                    MenuCategoryTab(
                        name = section.name,
                        count = section.products.size,
                        isActive = activeTabId == section.id,
                        onClick = {
                            scope.launch {
                                listState.animateScrollToItem(indexMap[section.id] ?: 0)
                            }
                        }
                    )
                }
            }
            HorizontalDivider(color = AccentBlue.copy(alpha = 0.10f))
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            sections.forEach { section ->
                item(key = "header_${section.id}") {
                    MenuCategoryHeader(
                        name = section.name,
                        count = section.products.size
                    )
                }
                items(section.products, key = { it.id }) { product ->
                    MenuProductCard(
                        product = product,
                        quantity = cartViewModel.quantityOf(product.id),
                        onAdd = { cartViewModel.add(product) },
                        onRemove = { cartViewModel.remove(product) },
                        onOpenPicker = { onOpenPicker(product) },
                        isShopOpen = isShopOpen
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(bottomPadding)) }
        }
    }
}

@Composable
private fun MenuCategoryTab(
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
