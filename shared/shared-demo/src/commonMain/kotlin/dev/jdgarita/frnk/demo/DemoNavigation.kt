package dev.jdgarita.frnk.demo

import dev.jdgarita.frnk.monetization.FeatureGate
import dev.jdgarita.frnk.ui.nav.FrnkNavOptions
import dev.jdgarita.frnk.ui.nav.FrnkNavigator
import dev.jdgarita.frnk.ui.nav.ToolkitRoute

/**
 * Options for switching between the three bottom-nav tab roots: pop up to the start ([DemoRoute.Home])
 * saving the leaving tab's back stack, restore the target tab's saved stack, and avoid stacking a
 * duplicate of the tab on top of itself. This is the idiomatic single-`NavHost` multiple-back-stack
 * bottom-nav recipe, so e.g. `Components → ComponentDetail` survives a round trip through another tab.
 */
val DemoTabSwitchOptions: FrnkNavOptions =
    FrnkNavOptions(
        popUpTo = FrnkNavOptions.PopUpTo(route = DemoRoute.Home, saveState = true),
        launchSingleTop = true,
        restoreState = true,
    )

/**
 * Compose-free routing of the demo's one-shot [DemoEffect]s. A [DemoEffect.Navigate] carrying
 * [FeatureGate.PAYWALL_ROUTE_KEY] is pushed onto the [navigator] as the toolkit-owned
 * [ToolkitRoute.Paywall] (the same route `rememberFrnkSettingsHandler` and `frnkPaywallDestination` use,
 * so every paywall entry point — Home crown, Settings, feature gates — lands on one destination); any
 * other key (or non-navigation effect) is forwarded to the host via [onForward]. Kept free of Compose so
 * it is unit-testable with a fake [FrnkNavigator] (see `DemoNavigationTest`).
 *
 * The `DemoViewModel` effect channel is single-consumer, so this must be invoked from exactly one
 * collector — the central `EffectCollector` above the `FrnkNavHost` in `DemoScreen`.
 */
fun routeDemoEffect(
    effect: DemoEffect,
    navigator: FrnkNavigator,
    onForward: (DemoEffect) -> Unit,
) {
    when (effect) {
        is DemoEffect.Navigate ->
            if (effect.routeKey == FeatureGate.PAYWALL_ROUTE_KEY) {
                navigator.navigate(ToolkitRoute.Paywall)
            } else {
                onForward(effect)
            }
        is DemoEffect.Toast -> onForward(effect)
    }
}
