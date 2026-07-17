package com.countertap.customer.ui.menu

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.countertap.customer.ui.theme.AccentBlue
import com.countertap.customer.ui.theme.BackgroundDark
import com.countertap.customer.ui.theme.TextPrimary
import com.countertap.customer.ui.theme.TextSecondary
import com.countertap.customer.viewmodel.CartViewModel
import com.countertap.shared.Product

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuBookLayout(
    sections: List<MenuSection>,
    cartViewModel: CartViewModel,
    isShopOpen: Boolean,
    onOpenPicker: (Product) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 96.dp,
    modifier: Modifier = Modifier
) {
    if (sections.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { sections.size })

    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val section = sections[page]
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
            ) {
                item(key = "chapter_${section.id}") {
                    MenuBookChapterHeader(
                        name = section.name,
                        count = section.products.size,
                        pageIndex = page + 1,
                        pageCount = sections.size
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
                item { Spacer(modifier = Modifier.height(bottomPadding)) }
            }
        }

        MenuBookPageIndicator(
            sectionName = sections[pagerState.currentPage].name,
            currentPage = pagerState.currentPage,
            pageCount = sections.size
        )
    }
}

@Composable
private fun MenuBookPageIndicator(
    sectionName: String,
    currentPage: Int,
    pageCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$sectionName  ·  ${currentPage + 1} of $pageCount",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentPage) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) AccentBlue
                            else AccentBlue.copy(alpha = 0.25f)
                        )
                )
            }
        }
        Text(
            text = "Swipe for more categories",
            fontSize = 11.sp,
            color = TextSecondary.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
