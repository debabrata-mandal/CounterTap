package com.countertap.business.repository

import com.countertap.shared.Table
import com.countertap.shared.TableSession
import com.countertap.shared.TableSessionStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
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

    fun listenToTables(tenantId: String): Flow<List<Table>> = callbackFlow {
        val listener = tablesRef(tenantId).addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.toObjects(Table::class.java) ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    suspend fun addTable(tenantId: String, name: String): String {
        val ref = tablesRef(tenantId).document()
        ref.set(Table(id = ref.id, name = name)).await()
        return ref.id
    }

    suspend fun deleteTable(tenantId: String, tableId: String) {
        tablesRef(tenantId).document(tableId).delete().await()
    }

    fun listenToOpenSessions(tenantId: String): Flow<List<TableSession>> = callbackFlow {
        val listener = sessionsRef(tenantId)
            .whereEqualTo("status", TableSessionStatus.OPEN)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.toObjects(TableSession::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun closeSession(tenantId: String, sessionId: String) {
        sessionsRef(tenantId).document(sessionId)
            .update(mapOf("status" to TableSessionStatus.CLOSED, "closedAt" to Date()))
            .await()
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
