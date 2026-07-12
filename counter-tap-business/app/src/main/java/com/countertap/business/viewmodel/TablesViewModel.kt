package com.countertap.business.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.business.repository.TablesRepository
import com.countertap.business.repository.TenantRepository
import com.countertap.shared.Table
import com.countertap.shared.TableSession
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TablesUiState(
    val isLoading: Boolean = true,
    val tables: List<Table> = emptyList(),
    val openSessions: List<TableSession> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingTable: Table? = null,
    val error: String? = null
)

@HiltViewModel
class TablesViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val tenantRepository: TenantRepository,
    private val tablesRepository: TablesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TablesUiState())
    val uiState: StateFlow<TablesUiState> = _uiState.asStateFlow()

    private var tenantId: String? = null

    init { load() }

    private fun load() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = TablesUiState(isLoading = false, error = "Not signed in")
            return
        }
        viewModelScope.launch {
            try {
                val shop = tenantRepository.getTenantByOwnerId(uid)
                if (shop == null) {
                    _uiState.value = TablesUiState(isLoading = false, error = "Shop not found")
                    return@launch
                }
                tenantId = shop.id
                _uiState.value = _uiState.value.copy(isLoading = false)
                launch {
                    try {
                        tablesRepository.listenToTables(shop.id).collect { tables ->
                            _uiState.value = _uiState.value.copy(tables = tables.sortedBy { it.name })
                        }
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                }
                launch {
                    try {
                        tablesRepository.listenToOpenSessions(shop.id).collect { sessions ->
                            _uiState.value = _uiState.value.copy(openSessions = sessions)
                        }
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = TablesUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun showAddDialog() { _uiState.value = _uiState.value.copy(showAddDialog = true) }
    fun hideAddDialog() { _uiState.value = _uiState.value.copy(showAddDialog = false) }
    fun showEditDialog(table: Table) { _uiState.value = _uiState.value.copy(editingTable = table) }
    fun hideEditDialog() { _uiState.value = _uiState.value.copy(editingTable = null) }

    fun addTable(name: String, description: String = "") {
        val tid = tenantId ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                tablesRepository.addTable(tid, name.trim(), description.trim())
                hideAddDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateTable(tableId: String, name: String, description: String) {
        val tid = tenantId ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                tablesRepository.updateTable(tid, tableId, name.trim(), description.trim())
                hideEditDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteTable(tableId: String) {
        val tid = tenantId ?: return
        viewModelScope.launch {
            try { tablesRepository.deleteTable(tid, tableId) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
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
