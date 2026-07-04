package com.countertap.customer.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveOrderRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("active_order", Context.MODE_PRIVATE)

    fun saveActiveOrder(tenantId: String, orderId: String, shopName: String) {
        prefs.edit()
            .putString("tenant_id", tenantId)
            .putString("order_id", orderId)
            .putString("shop_name", shopName)
            .apply()
    }

    fun getActiveOrder(): Triple<String, String, String>? {
        val tenantId = prefs.getString("tenant_id", null) ?: return null
        val orderId = prefs.getString("order_id", null) ?: return null
        val shopName = prefs.getString("shop_name", "") ?: ""
        return Triple(tenantId, orderId, shopName)
    }

    fun clearActiveOrder() {
        prefs.edit().clear().apply()
    }
}
