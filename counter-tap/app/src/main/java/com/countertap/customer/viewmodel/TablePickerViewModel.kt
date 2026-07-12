package com.countertap.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.customer.repository.TablesRepository
import com.countertap.shared.Table
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TablePickerState {
    object Loading : TablePickerState()
    object NoTables : TablePickerState()
    data class Ready(val tables: List<Table>) : TablePickerState()
    object Proceeding : TablePickerState()
    data class Error(val message: String) : TablePickerState()
}

@HiltViewModel
class TablePickerViewModel @Inject constructor(
    private val tablesRepository: TablesRepository,
    private val tableContextHolder: TableContextHolder
) : ViewModel() {

    private val _state = MutableStateFlow<TablePickerState>(TablePickerState.Loading)
    val state: StateFlow<TablePickerState> = _state.asStateFlow()

    private var tenantId: String = ""

    fun loadTables(tenantId: String) {
        if (this.tenantId != tenantId) tableContextHolder.clear() // new restaurant — discard stale selection
        if (this.tenantId == tenantId && _state.value !is TablePickerState.Loading) return
        this.tenantId = tenantId
        viewModelScope.launch {
            try {
                _state.value = TablePickerState.Loading
                val tables = tablesRepository.getTables(tenantId)
                _state.value = if (tables.isEmpty()) TablePickerState.NoTables
                               else TablePickerState.Ready(tables.sortedBy { it.name })
            } catch (e: Exception) {
                _state.value = TablePickerState.Error(e.message ?: "Failed to load tables")
            }
        }
    }

    fun pickTakeaway() {
        tableContextHolder.clear()
    }

    fun pickTable(table: Table) {
        val tid = tenantId.ifEmpty { return }
        viewModelScope.launch {
            try {
                val sessionId = tablesRepository.getOrCreateSession(tid, table.id, table.name)
                tableContextHolder.set(table.id, table.name, sessionId)
            } catch (_: Exception) {
                tableContextHolder.clear()
            }
        }
    }
}
