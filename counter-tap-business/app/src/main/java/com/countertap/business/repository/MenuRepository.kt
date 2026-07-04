package com.countertap.business.repository

import com.countertap.shared.Category
import com.countertap.shared.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun productsRef(tenantId: String) =
        firestore.collection("tenants").document(tenantId).collection("products")

    private fun categoriesRef(tenantId: String) =
        firestore.collection("tenants").document(tenantId).collection("categories")

    suspend fun getCategories(tenantId: String): List<Category> =
        categoriesRef(tenantId).orderBy("order").get().await()
            .toObjects(Category::class.java)

    suspend fun addCategory(tenantId: String, name: String): Category {
        val existing = getCategories(tenantId)
        val doc = categoriesRef(tenantId).document()
        val category = Category(id = doc.id, name = name, order = existing.size)
        doc.set(category).await()
        return category
    }

    suspend fun deleteCategory(tenantId: String, categoryId: String) {
        categoriesRef(tenantId).document(categoryId).delete().await()
    }

    suspend fun getProducts(tenantId: String): List<Product> =
        productsRef(tenantId).orderBy("categoryId").get().await()
            .toObjects(Product::class.java)

    suspend fun upsertProduct(tenantId: String, product: Product): Product {
        return if (product.id.isEmpty()) {
            val doc = productsRef(tenantId).document()
            val saved = product.copy(id = doc.id)
            doc.set(saved).await()
            saved
        } else {
            productsRef(tenantId).document(product.id).set(product).await()
            product
        }
    }

    suspend fun deleteProduct(tenantId: String, productId: String) {
        productsRef(tenantId).document(productId).delete().await()
    }

    suspend fun setProductAvailability(tenantId: String, productId: String, available: Boolean) {
        productsRef(tenantId).document(productId).update("available", available).await()
    }
}
