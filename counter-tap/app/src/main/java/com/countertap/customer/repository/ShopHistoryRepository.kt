package com.countertap.customer.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class ShopRecord(
    val tenantId: String,
    val name: String,
    val address: String
)

@Singleton
class ShopHistoryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("shop_history", Context.MODE_PRIVATE)
    private val KEY = "visited_shops"
    private val MAX = 5

    fun saveShop(tenantId: String, name: String, address: String) {
        val existing = getRecentShops().toMutableList()
        existing.removeAll { it.tenantId == tenantId }
        existing.add(0, ShopRecord(tenantId, name, address))
        if (existing.size > MAX) existing.subList(MAX, existing.size).clear()
        val array = JSONArray()
        existing.forEach { shop ->
            array.put(JSONObject().apply {
                put("tenantId", shop.tenantId)
                put("name", shop.name)
                put("address", shop.address)
            })
        }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    fun getRecentShops(): List<ShopRecord> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                ShopRecord(
                    tenantId = obj.getString("tenantId"),
                    name = obj.getString("name"),
                    address = obj.optString("address", "")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
