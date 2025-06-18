package com.tweener.kmpship.presentation._internal.launcher

/**
 * @author Vivien Mahe
 * @since 21/01/2025
 */

interface MobileStoreSubscriptionLauncher {

    fun open(packageName: String, productId: String? = null)
}
