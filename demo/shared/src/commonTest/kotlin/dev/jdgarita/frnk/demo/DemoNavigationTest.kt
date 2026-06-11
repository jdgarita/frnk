package dev.jdgarita.frnk.demo

import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.monetization.FeatureGate
import dev.jdgarita.frnk.ui.nav.ToolkitRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Contract test for the Compose-free [routeDemoEffect] mapping, exercised with a recording `navigate`
 * lambda: a navigation effect's `routeKey` must drive `navigate` to the matching typed route (not the
 * host); an unrecognized key and any non-navigation effect must be forwarded to the host (not navigate).
 */
class DemoNavigationTest {
    @Test
    fun navigate_effect_maps_known_route_key_to_typed_route_and_is_not_forwarded() {
        val navigated = mutableListOf<NavKey>()
        val forwarded = mutableListOf<DemoEffect>()

        // The key the demo's FeatureGate actually emits — assert it resolves to the toolkit-owned
        // ToolkitRoute.Paywall (the route registered via frnkPaywallNavigation) rather than the router
        // hardcoding a route regardless of payload.
        routeDemoEffect(DemoEffect.Navigate(FeatureGate.PAYWALL_ROUTE_KEY), navigated::add, forwarded::add)

        assertEquals(listOf<NavKey>(ToolkitRoute.Paywall), navigated)
        assertTrue(forwarded.isEmpty(), "recognized navigation effects must not be forwarded to the host")
    }

    @Test
    fun navigate_effect_with_unknown_route_key_is_forwarded_and_does_not_navigate() {
        val navigated = mutableListOf<NavKey>()
        val forwarded = mutableListOf<DemoEffect>()

        val effect = DemoEffect.Navigate("toolkit/does-not-exist")
        routeDemoEffect(effect, navigated::add, forwarded::add)

        assertEquals(listOf<DemoEffect>(effect), forwarded)
        assertTrue(navigated.isEmpty(), "an unrecognized route key must not drive navigation")
    }

    @Test
    fun toast_effect_is_forwarded_and_does_not_navigate() {
        val navigated = mutableListOf<NavKey>()
        val forwarded = mutableListOf<DemoEffect>()

        val toast = DemoEffect.Toast("hi")
        routeDemoEffect(toast, navigated::add, forwarded::add)

        assertEquals(listOf<DemoEffect>(toast), forwarded)
        assertTrue(navigated.isEmpty(), "toast effects must not drive navigation")
    }
}
