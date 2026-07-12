package com.countertap.business.viewmodel

import android.content.Context
import android.media.RingtoneManager
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.business.repository.OrderRepository
import com.countertap.business.repository.TablesRepository
import com.countertap.business.repository.TenantRepository
import com.countertap.shared.Order
import com.countertap.shared.OrderStatus
import com.countertap.shared.TableSession
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class OrdersUiState(
    val isLoading: Boolean = true,
    val activeOrders: List<Order> = emptyList(),
    val doneOrders: List<Order> = emptyList(),
    val openSessions: List<TableSession> = emptyList(),
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
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val tenantRepository: TenantRepository,
    private val orderRepository: OrderRepository,
    private val tablesRepository: TablesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private var tenantId: String? = null
    private var knownOrderIds = emptySet<String>()
    private var isFirstLoad = true
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        tts = TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) tts?.language = Locale.ENGLISH
        }
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
                launch {
                    try {
                        tablesRepository.listenToOpenSessions(shop.id).collect { sessions ->
                            _uiState.value = _uiState.value.copy(openSessions = sessions)
                        }
                    } catch (_: Exception) { /* non-fatal; tables feature is opt-in */ }
                }
                orderRepository.listenToOrders(shop.id).collect { orders ->
                    val active = orders
                        .filter { it.status in ACTIVE_STATUSES }
                        .sortedWith(compareBy({ STATUS_PRIORITY[it.status] ?: 9 }, { it.createdAt }))
                    val done = orders
                        .filter { it.status !in ACTIVE_STATUSES }
                        .sortedByDescending { it.createdAt }

                    val currentIds = orders.map { it.id }.toSet()
                    if (!isFirstLoad) {
                        val newOrders = orders.filter { it.id in (currentIds - knownOrderIds) }
                        if (newOrders.isNotEmpty()) alertNewOrder(newOrders)
                    }
                    isFirstLoad = false
                    knownOrderIds = currentIds

                    _uiState.value = _uiState.value.copy(isLoading = false, activeOrders = active, doneOrders = done)
                }
            } catch (e: Exception) {
                _uiState.value = OrdersUiState(isLoading = false, error = e.message ?: "Failed to load orders")
            }
        }
    }

    private fun alertNewOrder(newOrders: List<Order>) {
        // Play notification sound first
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(context, uri)?.play()
        } catch (_: Exception) {}

        // Speak each new order
        if (ttsReady) {
            newOrders.forEach { order ->
                val itemCount = order.items.sumOf { it.quantity }
                val name = order.customerName.ifBlank { "a customer" }
                val amount = order.totalAmount.toInt()
                val text = "New order from $name. $itemCount item${if (itemCount > 1) "s" else ""}. Total $amount rupees."
                tts?.speak(text, TextToSpeech.QUEUE_ADD, null, order.id)
            }
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }

    fun markAsPaid(orderId: String) {
        val tid = tenantId ?: return
        viewModelScope.launch {
            try {
                orderRepository.markAsPaid(tid, orderId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
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

    fun settleAndCloseSession(sessionId: String) {
        val tid = tenantId ?: return
        viewModelScope.launch {
            try { tablesRepository.settleAndCloseSession(tid, sessionId) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }
}
