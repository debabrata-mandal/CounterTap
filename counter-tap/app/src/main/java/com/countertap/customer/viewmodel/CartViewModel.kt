package com.countertap.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.customer.repository.ActiveOrderRepository
import com.countertap.customer.repository.CreditRepository
import com.countertap.customer.repository.OrderRepository
import com.countertap.customer.repository.ShopHistoryRepository
import com.countertap.customer.repository.TablesRepository
import com.countertap.shared.CreditLine
import com.countertap.shared.CreditLineStatus
import com.countertap.shared.Order
import com.countertap.shared.OrderItem
import com.countertap.shared.OrderStatus
import com.countertap.shared.PaymentMethod
import com.countertap.shared.PaymentStatus
import com.countertap.shared.Product
import com.countertap.shared.SelectedOption
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItem(
    val product: Product,
    val quantity: Int,
    val selectedOptions: List<SelectedOption> = emptyList(),
    val unitPrice: Double = product.price
)

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
    private val activeOrderRepository: ActiveOrderRepository,
    private val tablesRepository: TablesRepository,
    private val tableContextHolder: TableContextHolder,
    private val creditRepository: CreditRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val tableContext: StateFlow<TableContext?> = tableContextHolder.tableContext

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    private val _orderState = MutableStateFlow<OrderState>(OrderState.Idle)
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()

    private val _creditLine = MutableStateFlow<CreditLine?>(null)
    val creditLine: StateFlow<CreditLine?> = _creditLine.asStateFlow()

    private val _paymentMethod = MutableStateFlow(PaymentMethod.CASH)
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private var creditTenantId: String = ""
    private var creditJob: Job? = null

    val totalAmount: Double
        get() = _items.value.sumOf { it.unitPrice * it.quantity }

    val totalCount: Int
        get() = _items.value.sumOf { it.quantity }

    fun add(product: Product) = addConfigured(product, emptyList())

    fun remove(product: Product) = removeConfigured(product, emptyList())

    fun addConfigured(product: Product, selectedOptions: List<SelectedOption>) {
        val unitPrice = product.price + selectedOptions.sumOf { it.priceAddon }
        val current = _items.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == product.id && it.selectedOptions == selectedOptions }
        if (idx >= 0) current[idx] = current[idx].copy(quantity = current[idx].quantity + 1)
        else current.add(CartItem(product, 1, selectedOptions, unitPrice))
        _items.value = current
    }

    fun removeConfigured(product: Product, selectedOptions: List<SelectedOption>) {
        val current = _items.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == product.id && it.selectedOptions == selectedOptions }
        if (idx < 0) return
        if (current[idx].quantity > 1) current[idx] = current[idx].copy(quantity = current[idx].quantity - 1)
        else current.removeAt(idx)
        _items.value = current
    }

    fun quantityOf(productId: String): Int =
        _items.value.filter { it.product.id == productId }.sumOf { it.quantity }

    fun quantityOfConfigured(productId: String, selectedOptions: List<SelectedOption>): Int =
        _items.value.find { it.product.id == productId && it.selectedOptions == selectedOptions }?.quantity ?: 0

    fun loadCreditLine(tenantId: String) {
        if (creditTenantId == tenantId) return
        creditTenantId = tenantId
        creditJob?.cancel()
        creditJob = viewModelScope.launch {
            creditRepository.listenToCreditLine(tenantId).collect { line ->
                _creditLine.value = line
                if (line?.status != CreditLineStatus.ACTIVE) {
                    _paymentMethod.value = PaymentMethod.CASH
                }
            }
        }
    }

    fun requestCredit(tenantId: String, shopName: String) {
        viewModelScope.launch {
            try { creditRepository.requestCredit(tenantId, shopName) }
            catch (_: Exception) {}
        }
    }

    fun requestLimitIncrease(tenantId: String, amount: Double) {
        viewModelScope.launch {
            try { creditRepository.requestLimitIncrease(tenantId, amount) }
            catch (_: Exception) {}
        }
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun placeOrder(tenantId: String, note: String = "") {
        val user = auth.currentUser ?: return
        val items = _items.value
        if (items.isEmpty()) return
        val tableCtx = tableContextHolder.tableContext.value
        val useCredit = _paymentMethod.value == PaymentMethod.CREDIT
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
                            price = it.unitPrice,
                            selectedOptions = it.selectedOptions
                        )
                    },
                    totalAmount = totalAmount,
                    status = OrderStatus.PENDING,
                    paymentMethod = _paymentMethod.value,
                    paymentStatus = if (useCredit) PaymentStatus.PAID else PaymentStatus.UNPAID,
                    note = note,
                    tableSessionId = tableCtx?.sessionId ?: "",
                    tableName = tableCtx?.tableName ?: ""
                )
                val orderId = orderRepository.placeOrder(tenantId, order)
                if (tableCtx != null) {
                    try {
                        tablesRepository.addOrderToSession(tenantId, tableCtx.sessionId, orderId, totalAmount, user.uid)
                    } catch (_: Exception) {}
                }
                if (useCredit) {
                    try { creditRepository.applyOrderToCredit(tenantId, totalAmount) }
                    catch (_: Exception) {}
                }
                val shopName = shopHistory.getRecentShops().find { it.tenantId == tenantId }?.name ?: ""
                orderRepository.saveToUserHistory(user.uid, orderId, tenantId, shopName, order)
                activeOrderRepository.saveActiveOrder(tenantId, orderId, shopName)
                _items.value = emptyList()
                _paymentMethod.value = PaymentMethod.CASH
                _orderState.value = OrderState.Success(orderId)
            } catch (e: Exception) {
                _orderState.value = OrderState.Error(e.message ?: "Failed to place order")
            }
        }
    }

    fun resetOrderState() { _orderState.value = OrderState.Idle }
}
