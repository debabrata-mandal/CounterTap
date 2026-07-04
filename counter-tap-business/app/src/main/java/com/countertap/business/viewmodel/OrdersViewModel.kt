package com.countertap.business.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.business.repository.OrderRepository
import com.countertap.business.repository.TenantRepository
import com.countertap.shared.Order
import com.countertap.shared.OrderStatus
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersUiState(
    val isLoading: Boolean = true,
    val activeOrders: List<Order> = emptyList(),
    val doneOrders: List<Order> = emptyList(),
    val error: String? = null
)

private val STATUS_PRIORITY = mapOf(
    OrderStatus.PENDING to 0,
    OrderStatus.CONFIRMED to 1,
    OrderStatus.PREPARING to 2,
    OrderStatus.READY to 3,
    OrderStatus.COMPLETED to 4,
    OrderStatus.CANCELLED to 5
)

private val ACTIVE_STATUSES = setOf(
    OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val tenantRepository: TenantRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private var tenantId: String? = null

    init {
        loadOrders()
    }

    private fun loadOrders() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = OrdersUiState(isLoading = false, error = "Not signed in")
            return
        }
        viewModelScope.launch {
            try {
                val shop = tenantRepository.getTenantByOwnerId(uid)
                if (shop == null) {
                    _uiState.value = OrdersUiState(isLoading = false, error = "Shop not found")
                    return@launch
                }
                tenantId = shop.id
                orderRepository.listenToOrders(shop.id).collect { orders ->
                    val active = orders
                        .filter { it.status in ACTIVE_STATUSES }
                        .sortedWith(compareBy({ STATUS_PRIORITY[it.status] ?: 9 }, { it.createdAt }))
                    val done = orders
                        .filter { it.status !in ACTIVE_STATUSES }
                        .sortedByDescending { it.createdAt }
                    _uiState.value = OrdersUiState(isLoading = false, activeOrders = active, doneOrders = done)
                }
            } catch (e: Exception) {
                _uiState.value = OrdersUiState(isLoading = false, error = e.message ?: "Failed to load orders")
            }
        }
    }

    fun updateStatus(orderId: String, customerId: String, newStatus: String) {
        val tid = tenantId ?: return
        viewModelScope.launch {
            try {
                orderRepository.updateStatus(tid, orderId, customerId, newStatus)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
