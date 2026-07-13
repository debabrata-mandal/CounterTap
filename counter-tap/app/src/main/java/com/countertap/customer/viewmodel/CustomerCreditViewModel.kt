package com.countertap.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.customer.repository.CreditRepository
import com.countertap.shared.CreditLine
import com.countertap.shared.CreditLineStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerCreditViewModel @Inject constructor(
    private val creditRepository: CreditRepository
) : ViewModel() {

    private val _creditLines = MutableStateFlow<List<CreditLine>>(emptyList())
    val creditLines: StateFlow<List<CreditLine>> = _creditLines.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            creditRepository.listenToAllCreditLines().collect { lines ->
                _creditLines.value = lines.sortedWith(
                    compareBy<CreditLine> {
                        when (it.status) {
                            CreditLineStatus.ACTIVE -> 0
                            CreditLineStatus.PENDING -> 1
                            else -> 2
                        }
                    }.thenBy { it.shopName }
                )
                _isLoading.value = false
            }
        }
    }

    fun requestLimitIncrease(tenantId: String, amount: Double) {
        viewModelScope.launch {
            try { creditRepository.requestLimitIncrease(tenantId, amount) }
            catch (_: Exception) {}
        }
    }
}
