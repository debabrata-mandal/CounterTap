package com.countertap.business.repository

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
    fun listenToOrders(tenantId: String): Flow<List<Order>> = callbackFlow {
        val ref = firestore
            .collection("tenants")
            .document(tenantId)
            .collection("orders")

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val orders = snapshot.toObjects(Order::class.java)
                trySend(orders)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun markAsPaid(tenantId: String, orderId: String) {
        val orderRef = firestore.collection("tenants").document(tenantId)
            .collection("orders").document(orderId)
        val order = orderRef.get().await().toObject(Order::class.java)
        val batch = firestore.batch()
        batch.update(orderRef, "paymentStatus", "paid")
        if (order?.customerId?.isNotBlank() == true) {
            batch.update(
                firestore.collection("users").document(order.customerId)
                    .collection("orders").document(orderId),
                "paymentStatus", "paid"
            )
        }
        batch.commit().await()
    }

    suspend fun updateStatus(tenantId: String, orderId: String, customerId: String, newStatus: String) {
        val batch = firestore.batch()
        batch.update(
            firestore.collection("tenants").document(tenantId).collection("orders").document(orderId),
            "status", newStatus
        )
        if (customerId.isNotBlank()) {
            batch.update(
                firestore.collection("users").document(customerId).collection("orders").document(orderId),
                "status", newStatus
            )
        }
        batch.commit().await()
    }
}
