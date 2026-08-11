package dev.jdgarita.frnk.monetization

import dev.jdgarita.frnk.identity.IdentitySource
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.flow.StateFlow

/**
 * The toolkit's **single source of truth for Free vs Pro**, owned by frnk and independent of any
 * billing SDK. It wraps an [EntitlementProvider] (e.g. RevenueCat) and overlays a persisted **god mode**
 * override so a developer can force-Pro themselves — even in a production build — without purchasing.
 *
 * Features check access through here (usually via [FeatureGate]); the paywall drives purchase/restore
 * through here too. The default implementation is `DefaultEntitlementManager` (pure Kotlin), bound by
 * [monetizationModule].
 */
interface EntitlementManager : IdentitySource {
    /** Combined Free/Pro snapshot (provider OR god mode), with its [ProSource]. */
    val status: StateFlow<EntitlementStatus>

    /** Convenience view of [status]'s `isPro` for synchronous gating. */
    val isPro: StateFlow<Boolean>

    /** Whether the local god-mode override is currently active. */
    val isGodMode: StateFlow<Boolean>

    /** Enable/disable the persisted god-mode override (forces Pro regardless of the provider). */
    fun setGodMode(enabled: Boolean)

    /** Re-fetch entitlement state from the provider. */
    suspend fun refresh()

    /** Fetch purchasable plans for the paywall. */
    suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError>

    /** Purchase a plan by provider package id. */
    suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError>

    /** Restore previous purchases. */
    suspend fun restorePurchases(): AppResult<Boolean, MonetizationError>

    /**
     * Silently sync the store receipt with the billing provider (no store UI) — see
     * [EntitlementProvider.syncPurchases]. Returns whether the user is Pro afterwards.
     */
    suspend fun syncPurchases(): AppResult<Boolean, MonetizationError>

    /** The store's manage-subscriptions URL for the current customer, or `null` if none. */
    suspend fun managementUrl(): AppResult<String?, MonetizationError>

    suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError>
}