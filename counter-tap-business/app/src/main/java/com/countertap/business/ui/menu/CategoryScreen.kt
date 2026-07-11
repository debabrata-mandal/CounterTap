package com.countertap.business.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.countertap.business.ui.theme.AccentBlue
import com.countertap.business.ui.theme.BackgroundDark
import com.countertap.business.ui.theme.CardBackground
import com.countertap.business.ui.theme.CardElevated
import com.countertap.business.ui.theme.DividerColor
import com.countertap.business.ui.theme.TextPrimary
import com.countertap.business.ui.theme.TextSecondary
import com.countertap.business.viewmodel.MenuViewModel
import com.countertap.shared.Category
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onBack: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var newCategoryName by rememberSaveable { mutableStateOf("") }
    var categoryToDelete by rememberSaveable { mutableStateOf<String?>(null) }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete category?") },
            text = { Text("Products in this category will become uncategorized.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(categoryToDelete!!)
                    categoryToDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundDark)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            // Add category input — spans full width
            item(span = { GridItemSpan(2) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("New category name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentBlue,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                viewModel.addCategory(newCategoryName.trim())
                                newCategoryName = ""
                            }
                        },
                        enabled = newCategoryName.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }

            // Empty state — spans full width
            if (uiState.categories.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗂️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No categories yet", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Add one above to organise your menu", fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Category tiles
            items(uiState.categories, key = { it.id }) { category ->
                val productCount = uiState.products.count { it.categoryId == category.id }
                CategoryTile(
                    category = category,
                    productCount = productCount,
                    onDelete = { categoryToDelete = category.id }
                )
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: Category,
    productCount: Int,
    onDelete: () -> Unit
) {
    val emoji = categoryEmoji(category.name)
    val tileColor = tilePalette[abs(category.name.hashCode()) % tilePalette.size]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Emoji illustration area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(tileColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 52.sp)
            }

            // Label area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardElevated)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (productCount == 1) "1 item" else "$productCount items",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // Delete button — top-right overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(BackgroundDark.copy(alpha = 0.65f))
        ) {
            IconButton(onClick = onDelete, modifier = Modifier.fillMaxSize()) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = TextSecondary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

private val tilePalette = listOf(
    Color(0xFF5C6BC0), Color(0xFF26A69A), Color(0xFFEF5350),
    Color(0xFFFF7043), Color(0xFF66BB6A), Color(0xFFAB47BC),
    Color(0xFF26C6DA), Color(0xFF8D6E63), Color(0xFF78909C), Color(0xFFF4511E)
)

private fun categoryEmoji(name: String): String {
    val n = name.lowercase()
    return when {
        n.hasAny("tea", "chai")                               -> "☕"
        n.hasAny("coffee", "latte", "espresso", "cappuccino") -> "☕"
        n.hasAny("juice", "smoothie", "lassi")                -> "🥤"
        n.hasAny("shake", "milkshake")                        -> "🧋"
        n.hasAny("beverage", "drink", "soda", "cold drink")   -> "🧃"
        n.hasAny("water", "mineral")                          -> "💧"
        n.hasAny("snack", "appetizer", "starter", "finger")   -> "🍟"
        n.hasAny("biryani")                                   -> "🍚"
        n.hasAny("rice")                                      -> "🍚"
        n.hasAny("curry", "gravy", "masala")                  -> "🥘"
        n.hasAny("dal", "daal", "lentil", "soup")             -> "🥣"
        n.hasAny("main", "meal", "thali")                     -> "🍛"
        n.hasAny("roti", "naan", "paratha", "chapati", "bread") -> "🫓"
        n.hasAny("dessert", "sweet", "mithai", "halwa", "kheer") -> "🍮"
        n.hasAny("cake", "pastry", "brownie", "cookie", "muffin") -> "🎂"
        n.hasAny("ice cream", "icecream", "kulfi", "gelato")  -> "🍨"
        n.hasAny("chicken", "poultry", "wings")               -> "🍗"
        n.hasAny("mutton", "lamb", "kebab", "seekh")          -> "🍖"
        n.hasAny("fish", "seafood", "prawn", "shrimp", "crab") -> "🐟"
        n.hasAny("egg", "omelette", "omelette")               -> "🍳"
        n.hasAny("veg", "salad", "vegetable", "paneer")       -> "🥗"
        n.hasAny("pizza")                                     -> "🍕"
        n.hasAny("burger", "sandwich", "wrap", "roll")        -> "🍔"
        n.hasAny("pasta", "noodle", "chow", "hakka")          -> "🍜"
        n.hasAny("breakfast", "morning", "brunch")            -> "🍳"
        n.hasAny("combo", "set meal", "meal deal", "thali")   -> "🍱"
        n.hasAny("fruit", "fruits")                           -> "🍎"
        n.hasAny("special", "chef", "signature", "house")     -> "⭐"
        n.hasAny("pav", "vada", "dosa", "idli", "samosa")    -> "🫔"
        n.hasAny("momos", "dumpling", "dim sum")              -> "🥟"
        else                                                  -> "🍽️"
    }
}

private fun String.hasAny(vararg keywords: String) = keywords.any { this.contains(it) }
