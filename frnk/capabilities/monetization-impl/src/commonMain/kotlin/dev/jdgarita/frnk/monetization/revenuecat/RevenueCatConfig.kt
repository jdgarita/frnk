package dev.jdgarita.frnk.monetization.revenuecat

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
    val proEntitlementId: String = "pro"
)