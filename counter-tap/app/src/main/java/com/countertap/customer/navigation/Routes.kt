package com.countertap.customer.navigation

object Routes {
    const val SIGN_IN = "sign_in"
    const val SCANNER = "scanner"
    const val MENU = "menu/{tenantId}"
    const val CART = "cart/{tenantId}"
    const val ORDER_CONFIRMED = "order_confirmed/{orderId}"

    fun menu(tenantId: String) = "menu/$tenantId"
    fun cart(tenantId: String) = "cart/$tenantId"
    fun orderConfirmed(orderId: String) = "order_confirmed/$orderId"
}
