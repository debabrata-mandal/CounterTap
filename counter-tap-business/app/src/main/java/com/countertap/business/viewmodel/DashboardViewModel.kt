package com.countertap.business.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.business.repository.OrderRepository
import com.countertap.business.repository.TenantRepository
import com.countertap.shared.Order
import com.countertap.shared.OrderStatus
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardState(
    val isLoading: Boolean = true,
    val shopName: String = "",
    val todayRevenue: Int = 0,
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val totalOrders: Int = 0,
    val avgOrder: Int = 0,
    val activeOrders: List<Order> = emptyList(),
    val bestSellers: List<Pair<String, Int>> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val tenantRepository: TenantRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        val uid = auth.currentUser?.uid ?: run {
            _state.value = DashboardState(isLoading = false, error = "Not signed in")
            return
        }

        viewModelScope.launch {
            try {
                val shop = tenantRepository.getTenantByOwnerId(uid)
                if (shop == null) {
                    _state.value = DashboardState(isLoading = false, error = "Shop not found")
                    return@launch
                }

                _state.value = _state.value.copy(shopName = shop.name)

                orderRepository.listenToOrders(shop.id).collect { orders ->
                    val today = Calendar.getInstance()

                    fun isToday(order: Order): Boolean {
                        val date = order.createdAt ?: return false
                        val cal = Calendar.getInstance().apply { time = date }
                        return cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) &&
                               cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                               cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    }

                    val todayOrders = orders.filter { isToday(it) }
                    val completedToday = todayOrders.filter { it.status == OrderStatus.COMPLETED }
                    val pendingToday = todayOrders.filter { it.status == OrderStatus.PENDING }

                    val todayRevenue = completedToday.sumOf { it.totalAmount.toInt() }
                    val completedCount = completedToday.size
                    val pendingCount = pendingToday.size
                    val totalOrders = todayOrders.size
                    val avgOrder = if (completedCount > 0) todayRevenue / completedCount else 0

                    val activeStatuses = setOf(
                        OrderStatus.PENDING, OrderStatus.CONFIRMED,
                        OrderStatus.PREPARING, OrderStatus.READY
                    )
                    val statusOrder = mapOf(
                        OrderStatus.PENDING to 0,
                        OrderStatus.CONFIRMED to 1,
                        OrderStatus.PREPARING to 2,
                        OrderStatus.READY to 3
                    )
                    val activeOrders = orders
                        .filter { it.status in activeStatuses }
                        .sortedBy { statusOrder[it.status] ?: 99 }
                        .take(3)

                    val bestSellers = todayOrders
                        .flatMap { it.items }
                        .groupBy { it.productName }
                        .mapValues { (_, items) -> items.sumOf { it.quantity } }
                        .entries
                        .sortedByDescending { it.value }
                        .take(3)
                        .map { it.key to it.value }

                    _state.value = DashboardState(
                        isLoading = false,
                        shopName = shop.name,
                        todayRevenue = todayRevenue,
                        pendingCount = pendingCount,
                        completedCount = completedCount,
                        totalOrders = totalOrders,
                        avgOrder = avgOrder,
                        activeOrders = activeOrders,
                        bestSellers = bestSellers,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.value = DashboardState(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard"
                )
            }
        }
    }
}
