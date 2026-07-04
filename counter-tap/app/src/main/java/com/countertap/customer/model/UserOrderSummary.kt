package com.countertap.customer.model

import com.countertap.shared.OrderItem
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserOrderSummary(
    @DocumentId val id: String = "",
    val tenantId: String = "",
    val shopName: String = "",
    val status: String = "",
    val totalAmount: Double = 0.0,
    val items: List<OrderItem> = emptyList(),
    val note: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
