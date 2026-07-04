package com.countertap.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.customer.repository.ActiveOrderRepository
import com.countertap.customer.repository.OrderRepository
import com.countertap.customer.repository.ShopHistoryRepository
import com.countertap.customer.repository.ShopRecord
import com.countertap.shared.OrderStatus
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveOrderInfo(
    val tenantId: String,
    val orderId: String,
    val shopName: String,
    val status: String
)

data class CustomerHomeState(
    val recentShops: List<ShopRecord> = emptyList(),
    val userName: String = "",
    val activeOrder: ActiveOrderInfo? = null
)

@HiltViewModel
class CustomerHomeViewModel @Inject constructor(
    private val shopHistory: ShopHistoryRepository,
    private val auth: FirebaseAuth,
    private val activeOrderRepository: ActiveOrderRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CustomerHomeState())
    val state: StateFlow<CustomerHomeState> = _state.asStateFlow()

    private var activeOrderJob: Job? = null

    fun refresh() {
        _state.value = CustomerHomeState(
            recentShops = shopHistory.getRecentShops(),
            userName = auth.currentUser?.displayName?.substringBefore(" ") ?: "",
            activeOrder = _state.value.activeOrder
        )

        activeOrderJob?.cancel()

        val savedOrder = activeOrderRepository.getActiveOrder() ?: run {
            _state.value = _state.value.copy(activeOrder = null)
            return
        }

        val (tenantId, orderId, shopName) = savedOrder

        activeOrderJob = viewModelScope.launch {
            try {
                orderRepository.listenToOrder(tenantId, orderId).collect { order ->
                    if (order == null ||
                        order.status == OrderStatus.COMPLETED ||
                        order.status == OrderStatus.CANCELLED
                    ) {
                        activeOrderRepository.clearActiveOrder()
                        _state.value = _state.value.copy(activeOrder = null)
                    } else {
                        _state.value = _state.value.copy(
                            activeOrder = ActiveOrderInfo(
                                tenantId = tenantId,
                                orderId = orderId,
                                shopName = shopName,
                                status = order.status
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Silently fail — don't crash home screen if order tracking fails
            }
        }
    }
}
