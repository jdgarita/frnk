package dev.jdgarita.frnk.demo

import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.monetization.FeatureGate
import dev.jdgarita.frnk.ui.nav.ToolkitRoute

/**
 * Compose-free routing of the demo's one-shot [DemoEffect]s. A [DemoEffect.Navigate] carrying
 * [FeatureGate.PAYWALL_ROUTE_KEY] is pushed via [navigate] as the toolkit-owned [ToolkitRoute.Paywall]
 * (the same route `rememberFrnkSettingsHandler` and `frnkPaywallNavigation` use, so every paywall entry
 * point — Home crown, Settings, feature gates — lands on one destination); any other key (or a
 * non-navigation effect) is forwarded to the host via [onForward].
 *
 * [navigate] is a plain `(NavKey) -> Unit` (the host wires it to its `NavBackStack.navigateTo`), so this
 * stays free of Compose and the nav3 runtime types and is unit-testable with a recording lambda (see
 * `DemoNavigationTest`).
 *
 * The `DemoViewModel` effect channel is single-consumer, so this must be invoked from exactly one
 * collector — the central `EffectCollector` above the `FrnkNavDisplay` in `FrnkDemoApp`.
 */
fun routeDemoEffect(
    effect: DemoEffect,
    navigate: (NavKey) -> Unit,
    onForward: (DemoEffect) -> Unit,
) {
    when (effect) {
        is DemoEffect.Navigate ->
            when (effect.routeKey) {
                FeatureGate.PAYWALL_ROUTE_KEY -> navigate(ToolkitRoute.Paywall)
                else -> onForward(effect)
            }
        is DemoEffect.Toast -> onForward(effect)
    }
}
