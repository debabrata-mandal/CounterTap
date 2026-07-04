package com.countertap.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.customer.repository.OrderRepository
import com.countertap.customer.repository.ShopHistoryRepository
import com.countertap.customer.repository.ShopRecord
import com.countertap.shared.OrderStatus
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CustomerHomeState())
    val state: StateFlow<CustomerHomeState> = _state.asStateFlow()

    init {
        refresh()
        val uid = auth.currentUser?.uid
        if (uid != null) viewModelScope.launch {
            try {
                orderRepository.listenToUserOrders(uid).collect { orders ->
                    val active = orders
                        .filter { it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED }
                        .maxByOrNull { it.createdAt?.time ?: 0L }
                    _state.value = _state.value.copy(
                        activeOrder = active?.let {
                            ActiveOrderInfo(
                                tenantId = it.tenantId,
                                orderId = it.id,
                                shopName = it.shopName,
                                status = it.status
                            )
                        }
                    )
                }
            } catch (_: Exception) {}
        }
    }


    fun refresh() {
        _state.value = _state.value.copy(
            recentShops = shopHistory.getRecentShops(),
            userName = auth.currentUser?.displayName?.substringBefore(" ") ?: ""
        )
    }
}
