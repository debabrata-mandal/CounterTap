package com.countertap.business.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.business.repository.CreditRepository
import com.countertap.business.repository.TenantRepository
import com.countertap.shared.CreditLine
import com.countertap.shared.CreditLineStatus
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreditUiState(
    val isLoading: Boolean = true,
    val pendingLines: List<CreditLine> = emptyList(),
    val activeLines: List<CreditLine> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CreditViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val tenantRepository: TenantRepository,
    private val creditRepository: CreditRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreditUiState())
    val uiState: StateFlow<CreditUiState> = _uiState.asStateFlow()

    private var tenantId: String? = null

    init { load() }

    private fun load() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = CreditUiState(isLoading = false, error = "Not signed in")
            return
        }
        viewModelScope.launch {
            try {
                val shop = tenantRepository.getTenantByOwnerId(uid)
                if (shop == null) {
                    _uiState.value = CreditUiState(isLoading = false, error = "Shop not found")
                    return@launch
                }
                tenantId = shop.id
                _uiState.value = _uiState.value.copy(isLoading = false)
                launch {
                    try {
                        creditRepository.listenToCreditLines(shop.id).collect { lines ->
                            _uiState.value = _uiState.value.copy(
                                pendingLines = lines.filter { it.status == CreditLineStatus.PENDING }
                                    .sortedByDescending { it.requestedAt },
                                activeLines = lines.filter { it.status == CreditLineStatus.ACTIVE }
                                    .sortedBy { it.customerName }
                            )
                        }
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = CreditUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun approveCredit(customerId: String, limit: Double) {
        val tid = tenantId ?: return
        viewModelScope.launch {
            try { creditRepository.approveCredit(tid, customerId, limit) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun rejectCredit(customerId: String) {
        val tid = tenantId ?: return
        viewModelScope.launch {
            try { creditRepository.rejectCredit(tid, customerId) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun markSettled(customerId: String) {
        val tid = tenantId ?: return
        viewModelScope.launch {
            try { creditRepository.markSettled(tid, customerId) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun updateLimit(customerId: String, newLimit: Double) {
        val tid = tenantId ?: return
        viewModelScope.launch {
            try { creditRepository.updateLimit(tid, customerId, newLimit) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun approveLimitIncrease(customerId: String, increaseAmount: Double) {
        val tid = tenantId ?: return
        viewModelScope.launch {
            try { creditRepository.approveLimitIncrease(tid, customerId, increaseAmount) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun rejectLimitIncrease(customerId: String) {
        val tid = tenantId ?: return
        viewModelScope.launch {
            try { creditRepository.rejectLimitIncrease(tid, customerId) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }
}
