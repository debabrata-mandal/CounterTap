package com.countertap.customer.repository

import com.countertap.shared.Table
import com.countertap.shared.TableSession
import com.countertap.shared.TableSessionStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TablesRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun tablesRef(tenantId: String) =
        firestore.collection("tenants").document(tenantId).collection("tables")

    private fun sessionsRef(tenantId: String) =
        firestore.collection("tenants").document(tenantId).collection("tableSessions")

    suspend fun getTables(tenantId: String): List<Table> =
        tablesRef(tenantId).get().await().toObjects(Table::class.java)

    suspend fun getOrCreateSession(tenantId: String, tableId: String, tableName: String): String {
        val existing = sessionsRef(tenantId)
            .whereEqualTo("tableId", tableId)
            .whereEqualTo("status", TableSessionStatus.OPEN)
            .get().await()
        if (!existing.isEmpty) return existing.documents.first().id

        val ref = sessionsRef(tenantId).document()
        ref.set(TableSession(id = ref.id, tableId = tableId, tableName = tableName)).await()
        return ref.id
    }

    suspend fun addOrderToSession(
        tenantId: String,
        sessionId: String,
        orderId: String,
        amount: Double,
        customerId: String
    ) {
        val updates = mutableMapOf<String, Any>(
            "orderIds" to FieldValue.arrayUnion(orderId),
            "totalAmount" to FieldValue.increment(amount)
        )
        if (customerId.isNotBlank()) {
            updates["customerIds"] = FieldValue.arrayUnion(customerId)
        }
        sessionsRef(tenantId).document(sessionId).update(updates).await()
    }
}
