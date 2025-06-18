package com.tweener.kmpship.presentation._internal.libs.revenuecat

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

/**
 * @author Vivien Mahe
 * @since 14/01/2025
 */
abstract class RevenueCatConfiguration(
    private val isDebug: Boolean,
    private val apiKey: String,
) {

    fun init() {
        Purchases.logLevel = if (isDebug) LogLevel.DEBUG else LogLevel.ERROR
        Purchases.configure(configuration = PurchasesConfiguration(apiKey = apiKey))
        Purchases.simulatesAskToBuyInSandbox = isDebug
    }
}
