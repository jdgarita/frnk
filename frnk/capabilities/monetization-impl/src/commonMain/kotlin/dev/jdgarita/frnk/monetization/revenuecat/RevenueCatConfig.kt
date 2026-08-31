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
     * [ProMetadata.GENERIC]; hosts supply their own copy here — this is app copy, so it does
     * not belong in the toolkit. The provider is suspending and invoked on every metadata
     * fetch, so hosts can resolve localized resources (e.g. compose-resource `getString`)
     * against the *current* locale rather than baking one language in at DI time.
     */
    val paywallFallback: suspend () -> ProMetadata = { ProMetadata.GENERIC },
    /**
     * Template for the yearly plan's savings badge; the literal `%1$d` is replaced with the
     * whole-percent saving via `String.replace` (not a format call — keep the placeholder
     * verbatim, e.g. "Save %1$d%" → "Save 44%"). Suspending for the same reason as
     * [paywallFallback]: hosts localize by resolving a translated template per call.
     */
    val savingsBadgeTemplate: suspend () -> String = { "Save %1\$d%" }
)