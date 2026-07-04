package com.countertap.customer.repository

import com.countertap.shared.Category
import com.countertap.shared.Product
import com.countertap.shared.Shop
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getShop(tenantId: String): Shop? =
        firestore.collection("tenants").document(tenantId).get().await()
            .toObject(Shop::class.java)

    suspend fun getCategories(tenantId: String): List<Category> =
        firestore.collection("tenants").document(tenantId)
            .collection("categories").orderBy("order").get().await()
            .toObjects(Category::class.java)

    suspend fun getProducts(tenantId: String): List<Product> =
        firestore.collection("tenants").document(tenantId)
            .collection("products").whereEqualTo("available", true).get().await()
            .toObjects(Product::class.java)
}
