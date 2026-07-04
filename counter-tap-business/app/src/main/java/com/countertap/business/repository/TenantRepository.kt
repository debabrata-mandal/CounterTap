package com.countertap.business.repository

import com.countertap.shared.Shop
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TenantRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val tenantsCollection = firestore.collection("tenants")

    suspend fun getTenantByOwnerId(ownerId: String): Shop? {
        val snapshot = tenantsCollection
            .whereEqualTo("ownerId", ownerId)
            .limit(1)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.toObject(Shop::class.java)
    }

    suspend fun createTenant(shop: Shop): String {
        val doc = tenantsCollection.document()
        doc.set(shop.copy(id = doc.id)).await()
        return doc.id
    }

    suspend fun updateTenant(tenantId: String, fields: Map<String, Any>) {
        tenantsCollection.document(tenantId).update(fields).await()
    }
}
