package dev.jdgarita.frnk.monetization.usecase

import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProMetadata
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.utils.AppResult

/**
 * The paywall's **purchase use case** — the injectable seam through which the paywall loads offerings
 * and runs purchase/restore, so its ViewModel stays agnostic of the concrete
 * [EntitlementManager][dev.jdgarita.frnk.monetization.EntitlementManager]. Sibling of
 * [ObserveProStatusUseCase].
 *
 * Every operation returns [AppResult] (never throws). The default binding
 * ([DefaultPaywallPurchaseUseCase]) ships in
 * [monetizationModule][dev.jdgarita.frnk.monetization.monetizationModule] and simply delegates to the
 * canonical [EntitlementManager][dev.jdgarita.frnk.monetization.EntitlementManager] (which keeps the
 * analytics funnel).
 */
interface PaywallPurchaseUseCase {
    /** Fetch purchasable plans for the paywall. */
    suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError>

    suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError>

    /** Purchase a plan by provider package id. */
    suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError>

    /** Restore previous purchases; `true` if the customer is now Pro. */
    suspend fun restore(): AppResult<Boolean, MonetizationError>
}