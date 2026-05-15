package dev.jdgarita.frnk.monetization

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import kotlinx.coroutines.flow.StateFlow

/**
 * Gatekeeper used everywhere in the toolkit to decide whether to expose a Pro behaviour.
 *
 *   if (gate.canUse(Feature.UnlimitedExports)) doIt() else gate.requestUpgrade(...)
 *
 * `requestUpgrade` emits a generic "Paywall_Viewed" analytics event and returns a
 * route the caller can hand to the host's navigator. The toolkit never owns the paywall UI.
 */
class FeatureGate(
    private val entitlements: EntitlementManager,
    private val analytics: AnalyticsTracker,
) {
    val isPro: StateFlow<Boolean> get() = entitlements.isPro

    fun canUse(feature: Feature): Boolean = isPro.value || feature in freeFeatures

    /** Default fallback hook: log + return a route the host should navigate to. */
    fun requestUpgrade(source: String): String {
        analytics.track(ToolkitEvent.PaywallViewed, mapOf("source" to source))
        return PAYWALL_ROUTE_KEY
    }

    companion object {
        const val PAYWALL_ROUTE_KEY = "toolkit/paywall"

        /** Host can override by replacing the bound FeatureGate. */
        private val freeFeatures = emptySet<Feature>()
    }
}
