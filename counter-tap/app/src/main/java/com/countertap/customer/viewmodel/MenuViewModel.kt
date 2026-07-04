package com.countertap.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.customer.repository.ShopRepository
import com.countertap.shared.Category
import com.countertap.shared.Product
import com.countertap.shared.Shop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuUiState(
    val shop: Shop? = null,
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val repository: ShopRepository,
    private val shopHistory: com.countertap.customer.repository.ShopHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    fun loadShop(tenantId: String) {
        if (_uiState.value.shop?.id == tenantId) return
        viewModelScope.launch {
            _uiState.value = MenuUiState(isLoading = true)
            try {
                val shop = repository.getShop(tenantId)
                val categories = repository.getCategories(tenantId)
                val products = repository.getProducts(tenantId)
                _uiState.value = MenuUiState(
                    shop = shop,
                    categories = categories,
                    products = products
                )
                if (shop != null) {
                    shopHistory.saveShop(tenantId, shop.name, shop.address)
                }
            } catch (e: Exception) {
                _uiState.value = MenuUiState(error = e.message ?: "Failed to load menu")
            }
        }
    }
}
