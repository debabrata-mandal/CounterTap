package com.countertap.customer.viewmodel

import androidx.lifecycle.ViewModel
import com.countertap.customer.repository.ShopHistoryRepository
import com.countertap.customer.repository.ShopRecord
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CustomerHomeState(
    val recentShops: List<ShopRecord> = emptyList(),
    val userName: String = ""
)

@HiltViewModel
class CustomerHomeViewModel @Inject constructor(
    private val shopHistory: ShopHistoryRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(CustomerHomeState())
    val state: StateFlow<CustomerHomeState> = _state.asStateFlow()

    fun refresh() {
        _state.value = CustomerHomeState(
            recentShops = shopHistory.getRecentShops(),
            userName = auth.currentUser?.displayName?.substringBefore(" ") ?: ""
        )
    }
}
