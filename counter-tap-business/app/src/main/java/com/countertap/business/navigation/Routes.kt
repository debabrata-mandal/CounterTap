package com.countertap.business.navigation

object Routes {
    const val SIGN_IN     = "sign_in"
    const val LOADING     = "loading"
    const val SHOP_SETUP  = "shop_setup"
    const val UPI_SETUP   = "upi_setup"
    const val HOME        = "home"
    const val ADD_PRODUCT = "add_product"
    const val EDIT_PRODUCT = "edit_product/{productId}"
    const val CATEGORIES  = "categories"

    fun editProduct(productId: String) = "edit_product/$productId"
}
