package com.countertap.customer.repository

import com.countertap.shared.CreditLine
import com.countertap.shared.CreditLineStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreditRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun tenantCreditRef(tenantId: String, uid: String) =
        firestore.collection("tenants").document(tenantId)
            .collection("creditLines").document(uid)

    private fun userCreditRef(uid: String, tenantId: String) =
        firestore.collection("users").document(uid)
            .collection("creditLines").document(tenantId)

    fun listenToCreditLine(tenantId: String): Flow<CreditLine?> {
        val uid = auth.currentUser?.uid ?: return flowOf(null)
        return callbackFlow {
            val listener = userCreditRef(uid, tenantId).addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(if (snap != null && snap.exists()) snap.toObject(CreditLine::class.java) else null)
            }
            awaitClose { listener.remove() }
        }
    }

    suspend fun requestCredit(tenantId: String, shopName: String) {
        val user = auth.currentUser ?: return
        val creditLine = CreditLine(
            customerId = user.uid,
            customerName = user.displayName ?: "",
            customerEmail = user.email ?: "",
            tenantId = tenantId,
            shopName = shopName,
            status = CreditLineStatus.PENDING
        )
        val batch = firestore.batch()
        batch.set(tenantCreditRef(tenantId, user.uid), creditLine)
        batch.set(userCreditRef(user.uid, tenantId), creditLine)
        batch.commit().await()
    }

    suspend fun applyOrderToCredit(tenantId: String, amount: Double) {
        val uid = auth.currentUser?.uid ?: return
        val batch = firestore.batch()
        batch.update(tenantCreditRef(tenantId, uid), mapOf("balance" to FieldValue.increment(amount)))
        batch.update(userCreditRef(uid, tenantId), mapOf("balance" to FieldValue.increment(amount)))
        batch.commit().await()
    }
}
