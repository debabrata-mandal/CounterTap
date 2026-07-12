package com.countertap.customer.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class TableContext(
    val tableId: String,
    val tableName: String,
    val sessionId: String
)

@Singleton
class TableContextHolder @Inject constructor() {
    private val _tableContext = MutableStateFlow<TableContext?>(null)
    val tableContext: StateFlow<TableContext?> = _tableContext.asStateFlow()

    fun set(tableId: String, tableName: String, sessionId: String) {
        _tableContext.value = TableContext(tableId, tableName, sessionId)
    }

    fun clear() {
        _tableContext.value = null
    }
}
