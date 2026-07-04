package com.countertap.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.customer.repository.OrderRepository
import com.countertap.shared.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderTrackingState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val error: String? = null
)

@HiltViewModel
class OrderTrackingViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OrderTrackingState())
    val state: StateFlow<OrderTrackingState> = _state.asStateFlow()

    fun startTracking(tenantId: String, orderId: String) {
        viewModelScope.launch {
            try {
                orderRepository.listenToOrder(tenantId, orderId).collect { order ->
                    _state.value = OrderTrackingState(isLoading = false, order = order)
                }
            } catch (e: Exception) {
                _state.value = OrderTrackingState(isLoading = false, error = e.message ?: "Failed to track order")
            }
        }
    }
}
