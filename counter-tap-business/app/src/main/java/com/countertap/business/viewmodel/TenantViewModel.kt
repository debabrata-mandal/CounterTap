package com.countertap.business.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.business.repository.TenantRepository
import com.countertap.shared.Shop
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TenantState {
    object Loading : TenantState()
    object NeedsOnboarding : TenantState()
    data class Ready(val shop: Shop) : TenantState()
    data class Error(val message: String) : TenantState()
}

@HiltViewModel
class TenantViewModel @Inject constructor(
    private val repository: TenantRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _tenantState = MutableStateFlow<TenantState>(TenantState.Loading)
    val tenantState: StateFlow<TenantState> = _tenantState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    init {
        loadTenant()
    }

    fun loadTenant() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _tenantState.value = TenantState.Loading
            try {
                val shop = repository.getTenantByOwnerId(uid)
                _tenantState.value = if (shop != null) TenantState.Ready(shop) else TenantState.NeedsOnboarding
            } catch (e: Exception) {
                _tenantState.value = TenantState.Error(e.message ?: "Failed to load shop")
            }
        }
    }

    fun createShop(name: String, address: String, phone: String) {
        val uid = auth.currentUser?.uid ?: return
        val displayName = auth.currentUser?.displayName ?: ""
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                val shop = Shop(ownerId = uid, name = name, address = address, phone = phone)
                val tenantId = repository.createTenant(shop)
                _tenantState.value = TenantState.Ready(shop.copy(id = tenantId))
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Failed to create shop")
            }
        }
    }

    fun saveUpiId(upiId: String) {
        val shop = (_tenantState.value as? TenantState.Ready)?.shop ?: return
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                repository.updateTenant(shop.id, mapOf("upiId" to upiId))
                _tenantState.value = TenantState.Ready(shop.copy(upiId = upiId))
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Failed to save UPI ID")
            }
        }
    }

    fun toggleShopActive() {
        val shop = (_tenantState.value as? TenantState.Ready)?.shop ?: return
        val newActive = !shop.active
        viewModelScope.launch {
            try {
                repository.updateTenant(shop.id, mapOf("active" to newActive))
                _tenantState.value = TenantState.Ready(shop.copy(active = newActive))
            } catch (_: Exception) {}
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}
