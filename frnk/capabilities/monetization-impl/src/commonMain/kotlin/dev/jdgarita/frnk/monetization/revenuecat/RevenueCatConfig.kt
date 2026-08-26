package dev.jdgarita.frnk.monetization.revenuecat

import dev.jdgarita.frnk.monetization.ProMetadata

/**
 * Host-configurable RevenueCat knobs.
 *
 * A user is considered **Pro** when the RevenueCat entitlement identified by [proEntitlementId] is
 * active (`customerInfo.entitlements[proEntitlementId]?.isActive == true`). Hosts whose dashboard
 * uses a different identifier override the binding via Koin:
 *
 * ```
 * startKoin {
 *     allowOverride(true)
 *     modules(module { single { RevenueCatConfig(proEntitlementId = "premium") } })
 * }
 * ```
 */
data class RevenueCatConfig(
    val proEntitlementId: String = "pro",
    /**
     * Paywall copy used when the current offering carries no `title`/`subtitle`/`benefits`
     * metadata (offline, or an unconfigured dashboard). Defaults to the brand-free
     * [ProMetadata.GENERIC]; hosts supply their own (already-localized) copy here — this is
     * app copy, so it does not belong in the toolkit.
     */
    val paywallFallback: ProMetadata = ProMetadata.GENERIC,
    /**
     * Template for the yearly plan's savings badge; `%1$d` is replaced with the whole-percent
     * saving (e.g. "Save %1$d%" → "Save 44%"). Hosts localize by overriding the template.
     */
    val savingsBadgeTemplate: String = "Save %1\$d%"
)