package com.countertap.shared

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val categoryId: String = "",
    val available: Boolean = true,
    @ServerTimestamp val createdAt: Date? = null
)

data class Category(
    @DocumentId val id: String = "",
    val name: String = "",
    val order: Int = 0
)

data class Order(
    @DocumentId val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = OrderStatus.PENDING,
    val paymentStatus: String = PaymentStatus.UNPAID,
    val paymentMethod: String = PaymentMethod.CASH,
    val upiTransactionId: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Date? = null
)

object PaymentStatus {
    const val UNPAID = "unpaid"
    const val PAID = "paid"
}

object PaymentMethod {
    const val UPI = "upi"
    const val CASH = "cash"
}

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
)

object OrderStatus {
    const val PENDING = "PENDING"
    const val CONFIRMED = "CONFIRMED"
    const val PREPARING = "PREPARING"
    const val READY = "READY"
    const val COMPLETED = "COMPLETED"
    const val CANCELLED = "CANCELLED"
}

data class Shop(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val upiId: String = "",
    val logoUrl: String = "",
    val active: Boolean = true
)
