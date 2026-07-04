package com.countertap.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.customer.model.UserOrderSummary
import com.countertap.customer.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _orders = MutableStateFlow<List<UserOrderSummary>>(emptyList())
    val orders: StateFlow<List<UserOrderSummary>> = _orders.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { loadHistory() }

    private fun loadHistory() {
        val uid = auth.currentUser?.uid ?: run { _isLoading.value = false; return }
        viewModelScope.launch {
            try {
                orderRepository.listenToUserOrders(uid).collect { orders ->
                    _orders.value = orders
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}
