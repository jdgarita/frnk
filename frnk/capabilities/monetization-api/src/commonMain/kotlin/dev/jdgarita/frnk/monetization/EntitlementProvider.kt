package dev.jdgarita.frnk.monetization

import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.flow.StateFlow

/**
 * The pluggable billing backend behind the toolkit's Free/Pro layer. RevenueCat
 * (`shared-monetization-revenuecat`) and the demo fake implement this; feature code never touches it
 * directly — it goes through [EntitlementManager], which overlays god mode and persistence.
 *
 * Every method returns [AppResult] (or updates [isPro]); nothing throws.
 */
interface EntitlementProvider {
    /** The provider's own purchased-entitlement state (no god-mode overlay). */
    val isPro: StateFlow<Boolean>

    /** Re-fetch the latest customer info from the store/backend. */
    suspend fun refresh()

    /** Fetch the current offering's purchasable plans. */
    suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError>

    /** Purchase the plan with the given provider package [productId]. Returns whether the user is now Pro. */
    suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError>

    /** Restore previous purchases. Returns whether the user is now Pro. */
    suspend fun restore(): AppResult<Boolean, MonetizationError>

    /**
     * The platform's manage-subscriptions URL for the current customer (App Store / Play Store
     * subscriptions page), or `null` if there's nothing to manage. Hosts open it to let users
     * cancel/change their subscription — the store owns that UI, not the app.
     */
    suspend fun managementUrl(): AppResult<String?, MonetizationError>

    suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError>
}