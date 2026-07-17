package com.countertap.customer.ui.menu

import com.countertap.shared.Category
import com.countertap.shared.Product

data class MenuSection(
    val id: String,
    val name: String,
    val products: List<Product>
)

fun buildMenuSections(
    categories: List<Category>,
    products: List<Product>
): List<MenuSection> {
    val available = products.filter { it.available }
    val grouped = available.groupBy { it.categoryId }
    val knownCategoryIds = categories.map { it.id }.toSet()

    val sections = categories.mapNotNull { category ->
        val items = grouped[category.id] ?: emptyList()
        if (items.isEmpty()) null else MenuSection(category.id, category.name, items)
    }

    val uncategorized = available.filter { it.categoryId !in knownCategoryIds }
    return if (uncategorized.isNotEmpty()) {
        sections + MenuSection("__other__", "Other", uncategorized)
    } else {
        sections
    }
}
