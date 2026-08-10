package dev.jdgarita.frnk.monetization.usecase

import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProMetadata
import dev.jdgarita.frnk.utils.AppResult

/**
 * Default [PaywallPurchaseUseCase] — delegates each operation to the canonical [EntitlementManager]
 * (which owns the analytics funnel). Bound by
 * [monetizationModule][dev.jdgarita.frnk.monetization.monetizationModule] when monetization is installed.
 */
internal class DefaultPaywallPurchaseUseCase(
    private val entitlements: EntitlementManager
) : PaywallPurchaseUseCase {
    override suspend fun offerings() = entitlements.offerings()

    override suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError> = entitlements.fetchMetadata()

    override suspend fun purchase(productId: String) = entitlements.purchase(productId)

    override suspend fun restore() = entitlements.restorePurchases()

    override suspend fun sync() = entitlements.syncPurchases()
}