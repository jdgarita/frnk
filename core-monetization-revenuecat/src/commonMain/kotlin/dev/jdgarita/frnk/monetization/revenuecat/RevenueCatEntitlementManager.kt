package dev.jdgarita.frnk.monetization.revenuecat

import dev.jdgarita.frnk.monetization.EntitlementManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** TODO: wire to com.revenuecat.purchases.kmp.Purchases. Skeleton kept callable. */
internal class RevenueCatEntitlementManager : EntitlementManager {
    private val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    override suspend fun refresh() { /* call Purchases.sharedInstance.getCustomerInfo() */ }

    override suspend fun restorePurchases(): Boolean = false
}
