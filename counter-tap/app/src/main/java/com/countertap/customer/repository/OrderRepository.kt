package com.countertap.customer.repository

import com.countertap.shared.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun placeOrder(tenantId: String, order: Order): String {
        val doc = firestore.collection("tenants").document(tenantId)
            .collection("orders").document()
        doc.set(order.copy(id = doc.id)).await()
        return doc.id
    }

    fun listenToOrder(tenantId: String, orderId: String): Flow<Order?> = callbackFlow {
        val ref = firestore
            .collection("tenants").document(tenantId)
            .collection("orders").document(orderId)
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snapshot?.toObject(Order::class.java))
        }
        awaitClose { listener.remove() }
    }
}
