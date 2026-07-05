package com.countertap.customer.repository

import com.countertap.customer.model.UserOrderSummary
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

    suspend fun getOrder(tenantId: String, orderId: String): com.countertap.shared.Order? =
        firestore.collection("tenants").document(tenantId)
            .collection("orders").document(orderId).get().await()
            .toObject(com.countertap.shared.Order::class.java)

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

    suspend fun saveToUserHistory(
        userId: String,
        orderId: String,
        tenantId: String,
        shopName: String,
        order: Order
    ) {
        val summary = UserOrderSummary(
            id = orderId,
            tenantId = tenantId,
            shopName = shopName,
            status = order.status,
            totalAmount = order.totalAmount,
            items = order.items,
            note = order.note
        )
        firestore.collection("users").document(userId)
            .collection("orders").document(orderId)
            .set(summary)
            .await()
    }

    suspend fun markPaymentPaid(tenantId: String, orderId: String, txId: String) {
        val ref = firestore.collection("tenants").document(tenantId)
            .collection("orders").document(orderId)
        ref.update(
            mapOf(
                "paymentStatus" to "paid",
                "upiTransactionId" to txId
            )
        ).await()
        // also update user order summary
        val snapshot = ref.get().await()
        val order = snapshot.toObject(com.countertap.shared.Order::class.java) ?: return
        firestore.collection("users").document(order.customerId)
            .collection("orders").document(orderId)
            .update(mapOf("paymentStatus" to "paid"))
            .await()
    }

    fun listenToUserOrders(userId: String): Flow<List<UserOrderSummary>> = callbackFlow {
        val ref = firestore
            .collection("users").document(userId)
            .collection("orders")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val orders = snapshot.toObjects(UserOrderSummary::class.java)
                    .sortedWith(compareByDescending { it.createdAt })
                trySend(orders)
            }
        }
        awaitClose { listener.remove() }
    }
}
