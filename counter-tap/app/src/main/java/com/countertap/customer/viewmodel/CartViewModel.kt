package com.countertap.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.customer.repository.OrderRepository
import com.countertap.customer.repository.ShopHistoryRepository
import com.countertap.shared.Order
import com.countertap.shared.OrderItem
import com.countertap.shared.OrderStatus
import com.countertap.shared.Product
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItem(val product: Product, val quantity: Int)

sealed class OrderState {
    object Idle : OrderState()
    object Placing : OrderState()
    data class Success(val orderId: String) : OrderState()
    data class Error(val message: String) : OrderState()
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val shopHistory: ShopHistoryRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    private val _orderState = MutableStateFlow<OrderState>(OrderState.Idle)
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()

    val totalAmount: Double
        get() = _items.value.sumOf { it.product.price * it.quantity }

    val totalCount: Int
        get() = _items.value.sumOf { it.quantity }

    fun add(product: Product) {
        val current = _items.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == product.id }
        if (idx >= 0) current[idx] = current[idx].copy(quantity = current[idx].quantity + 1)
        else current.add(CartItem(product, 1))
        _items.value = current
    }

    fun remove(product: Product) {
        val current = _items.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == product.id }
        if (idx < 0) return
        if (current[idx].quantity > 1) current[idx] = current[idx].copy(quantity = current[idx].quantity - 1)
        else current.removeAt(idx)
        _items.value = current
    }

    fun quantityOf(productId: String): Int =
        _items.value.find { it.product.id == productId }?.quantity ?: 0

    fun placeOrder(tenantId: String, note: String = "") {
        val user = auth.currentUser ?: return
        val items = _items.value
        if (items.isEmpty()) return
        viewModelScope.launch {
            _orderState.value = OrderState.Placing
            try {
                val order = Order(
                    customerId = user.uid,
                    customerName = user.displayName ?: "",
                    items = items.map {
                        OrderItem(
                            productId = it.product.id,
                            productName = it.product.name,
                            quantity = it.quantity,
                            price = it.product.price
                        )
                    },
                    totalAmount = totalAmount,
                    status = OrderStatus.PENDING,
                    note = note
                )
                val orderId = orderRepository.placeOrder(tenantId, order)
                val shopName = shopHistory.getRecentShops().find { it.tenantId == tenantId }?.name ?: ""
                orderRepository.saveToUserHistory(user.uid, orderId, tenantId, shopName, order)
                _items.value = emptyList()
                _orderState.value = OrderState.Success(orderId)
            } catch (e: Exception) {
                _orderState.value = OrderState.Error(e.message ?: "Failed to place order")
            }
        }
    }

    fun resetOrderState() { _orderState.value = OrderState.Idle }
}
