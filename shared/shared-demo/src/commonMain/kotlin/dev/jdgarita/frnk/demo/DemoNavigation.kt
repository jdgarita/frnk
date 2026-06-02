package dev.jdgarita.frnk.demo

import dev.jdgarita.frnk.monetization.FeatureGate
import dev.jdgarita.frnk.ui.nav.FrnkNavOptions
import dev.jdgarita.frnk.ui.nav.FrnkNavigator

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
 * Map a `DemoEffect.Navigate.routeKey` (the host-navigable route string a `FeatureGate` hands back, e.g.
 * [FeatureGate.PAYWALL_ROUTE_KEY]) to a typed [DemoRoute]. Unknown keys return `null` so the caller can
 * surface them to the host rather than silently mis-routing.
 */
fun demoRouteForKey(routeKey: String): DemoRoute? =
    when (routeKey) {
        FeatureGate.PAYWALL_ROUTE_KEY -> DemoRoute.Paywall
        else -> null
    }

/**
 * Compose-free routing of the demo's one-shot [DemoEffect]s. A [DemoEffect.Navigate] is resolved to a
 * typed [DemoRoute] by its `routeKey` and pushed onto the [navigator]; an unrecognized key (or any
 * non-navigation effect) is forwarded to the host via [onForward]. Kept free of Compose so it is
 * unit-testable with a fake [FrnkNavigator] (see `DemoNavigationTest`).
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
            demoRouteForKey(effect.routeKey)?.let(navigator::navigate) ?: onForward(effect)
        is DemoEffect.Toast -> onForward(effect)
    }
}
