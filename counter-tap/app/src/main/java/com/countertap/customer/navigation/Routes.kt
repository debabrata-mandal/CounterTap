package com.countertap.customer.navigation

object Routes {
    const val SIGN_IN = "sign_in"
    const val HOME = "home"
    const val SCANNER = "scanner"
    const val MENU = "menu/{tenantId}"
    const val CART = "cart/{tenantId}"
    const val ORDER_TRACKING = "order_tracking/{tenantId}/{orderId}"
    const val ORDER_HISTORY = "order_history"

    fun menu(tenantId: String) = "menu/$tenantId"
    fun cart(tenantId: String) = "cart/$tenantId"
    fun orderTracking(tenantId: String, orderId: String) = "order_tracking/$tenantId/$orderId"
}
