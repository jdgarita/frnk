package dev.jdgarita.frnk.monetization

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Gatekeeper used everywhere in the toolkit to decide whether to expose a Pro behaviour.
 *
 *   if (gate.canUse(Feature.UnlimitedExports)) doIt() else gate.requestUpgrade(...)
 *
 * Reads the frnk-owned [EntitlementManager] (so god mode + any future Pro source are honored
 * automatically). [requestUpgrade] emits a generic "Paywall_Viewed" analytics event and returns the
 * toolkit paywall route key the caller hands to its navigator.
 *
 * @param freeFeatures features always available to Free users (host-configurable).
 */
class FeatureGate(
    private val entitlements: EntitlementManager,
    private val analytics: AnalyticsTracker,
    private val freeFeatures: Set<Feature> = emptySet(),
) {
    val isPro: StateFlow<Boolean> get() = entitlements.isPro

    /** Synchronous check against the current snapshot. */
    fun canUse(feature: Feature): Boolean = isPro.value || feature in freeFeatures

    /** Reactive variant for gating UI that should update when entitlement state changes. */
    fun observe(feature: Feature): Flow<Boolean> = entitlements.isPro.map { pro -> pro || feature in freeFeatures }

    /** Default fallback hook: log + return a route the host should navigate to. */
    fun requestUpgrade(source: String): String {
        analytics.track(ToolkitEvent.PaywallViewed, mapOf("source" to source))
        return PAYWALL_ROUTE_KEY
    }

    companion object {
        const val PAYWALL_ROUTE_KEY = "toolkit/paywall"
    }
}
