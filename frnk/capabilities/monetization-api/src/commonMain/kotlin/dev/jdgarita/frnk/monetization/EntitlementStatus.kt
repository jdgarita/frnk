package dev.jdgarita.frnk.monetization

import kotlin.time.Instant

/** Where a user's Pro access comes from. [GodMode] is a local override (testing), independent of the provider. */
enum class ProSource { None, Purchase, GodMode }

/**
 * The toolkit's canonical Free-vs-Pro snapshot.
 *
 * The trial / expiry fields describe the **purchased** Pro entitlement and are only populated when
 * [source] is [ProSource.Purchase]; Free and [ProSource.GodMode] always report no trial, no
 * expiration and no renewal. Their defaults preserve the pre-0.13 shape, so existing constructor
 * calls keep compiling.
 *
 * @property isTrial the active Pro entitlement is in a store free-trial period.
 * @property expiresAt when the active Pro entitlement (or its trial) ends; `null` for lifetime access,
 *   Free and god mode.
 * @property willRenew the store will renew the entitlement at [expiresAt] (false once the user has
 *   cancelled, for lifetime access, Free and god mode).
 */
data class EntitlementStatus(
    val isPro: Boolean,
    val source: ProSource,
    val isTrial: Boolean = false,
    val expiresAt: Instant? = null,
    val willRenew: Boolean = false
) {
    companion object {
        val Free = EntitlementStatus(isPro = false, source = ProSource.None)
    }
}