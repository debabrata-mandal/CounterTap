package com.countertap.business.repository

import com.countertap.shared.CreditLine
import com.countertap.shared.CreditLineStatus
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
class CreditRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun tenantCreditsRef(tenantId: String) =
        firestore.collection("tenants").document(tenantId).collection("creditLines")

    private fun userCreditRef(customerId: String, tenantId: String) =
        firestore.collection("users").document(customerId)
            .collection("creditLines").document(tenantId)

    fun listenToCreditLines(tenantId: String): Flow<List<CreditLine>> = callbackFlow {
        val listener = tenantCreditsRef(tenantId).addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.toObjects(CreditLine::class.java) ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    suspend fun approveCredit(tenantId: String, customerId: String, limit: Double) {
        val updates = mapOf(
            "status" to CreditLineStatus.ACTIVE,
            "limit" to limit,
            "balance" to 0.0,
            "approvedAt" to Date()
        )
        val batch = firestore.batch()
        batch.update(tenantCreditsRef(tenantId).document(customerId), updates)
        batch.update(userCreditRef(customerId, tenantId), updates)
        batch.commit().await()
    }

    suspend fun rejectCredit(tenantId: String, customerId: String) {
        val updates = mapOf("status" to CreditLineStatus.REJECTED)
        val batch = firestore.batch()
        batch.update(tenantCreditsRef(tenantId).document(customerId), updates)
        batch.update(userCreditRef(customerId, tenantId), updates)
        batch.commit().await()
    }

    suspend fun markSettled(tenantId: String, customerId: String, amount: Double) {
        val decrement = FieldValue.increment(-amount)
        val batch = firestore.batch()
        batch.update(tenantCreditsRef(tenantId).document(customerId), mapOf("balance" to decrement))
        batch.update(userCreditRef(customerId, tenantId), mapOf("balance" to decrement))
        batch.commit().await()
    }
}
