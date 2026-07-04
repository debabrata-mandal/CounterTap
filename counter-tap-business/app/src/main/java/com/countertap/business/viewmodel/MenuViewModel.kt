package com.countertap.business.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.business.repository.MenuRepository
import com.countertap.business.repository.TenantRepository
import com.countertap.shared.Category
import com.countertap.shared.Product
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuUiState(
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val tenantRepository: TenantRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private var tenantId: String? = null

    init {
        loadMenu()
    }

    fun loadMenu() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val shop = tenantRepository.getTenantByOwnerId(uid)
                tenantId = shop?.id
                val id = tenantId ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }
                val categories = menuRepository.getCategories(id)
                val products = menuRepository.getProducts(id)
                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    products = products,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun addCategory(name: String) {
        val id = tenantId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val category = menuRepository.addCategory(id, name)
                _uiState.value = _uiState.value.copy(
                    categories = _uiState.value.categories + category,
                    isSaving = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        val id = tenantId ?: return
        viewModelScope.launch {
            try {
                menuRepository.deleteCategory(id, categoryId)
                _uiState.value = _uiState.value.copy(
                    categories = _uiState.value.categories.filter { it.id != categoryId }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun saveProduct(product: Product) {
        val id = tenantId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val saved = menuRepository.upsertProduct(id, product)
                val updated = _uiState.value.products.toMutableList()
                val idx = updated.indexOfFirst { it.id == saved.id }
                if (idx >= 0) updated[idx] = saved else updated.add(saved)
                _uiState.value = _uiState.value.copy(products = updated, isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun deleteProduct(productId: String) {
        val id = tenantId ?: return
        viewModelScope.launch {
            try {
                menuRepository.deleteProduct(id, productId)
                _uiState.value = _uiState.value.copy(
                    products = _uiState.value.products.filter { it.id != productId }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleAvailability(product: Product) {
        val id = tenantId ?: return
        viewModelScope.launch {
            val newAvail = !product.available
            try {
                menuRepository.setProductAvailability(id, product.id, newAvail)
                _uiState.value = _uiState.value.copy(
                    products = _uiState.value.products.map {
                        if (it.id == product.id) it.copy(available = newAvail) else it
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
