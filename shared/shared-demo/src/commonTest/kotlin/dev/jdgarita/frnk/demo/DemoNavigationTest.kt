package dev.jdgarita.frnk.demo

import dev.jdgarita.frnk.monetization.FeatureGate
import dev.jdgarita.frnk.ui.nav.FrnkNavOptions
import dev.jdgarita.frnk.ui.nav.FrnkNavigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Contract test for the Compose-free [routeDemoEffect] mapping, exercised with a recording
 * [FrnkNavigator] fake: a navigation effect's `routeKey` must drive the navigator to the matching
 * typed route (not the host); an unrecognized key and any non-navigation effect must be forwarded to
 * the host (not the navigator).
 */
class DemoNavigationTest {
    @Test
    fun navigate_effect_maps_known_route_key_to_typed_route_and_is_not_forwarded() {
        val navigator = RecordingNavigator()
        val forwarded = mutableListOf<DemoEffect>()

        // The key the demo's FeatureGate actually emits — assert it resolves to the typed route rather
        // than the router hardcoding Paywall regardless of payload.
        routeDemoEffect(DemoEffect.Navigate(FeatureGate.PAYWALL_ROUTE_KEY), navigator, forwarded::add)

        assertEquals(listOf<Any>(DemoRoute.Paywall), navigator.navigated)
        assertTrue(forwarded.isEmpty(), "recognized navigation effects must not be forwarded to the host")
    }

    @Test
    fun navigate_effect_with_unknown_route_key_is_forwarded_and_does_not_navigate() {
        val navigator = RecordingNavigator()
        val forwarded = mutableListOf<DemoEffect>()

        val effect = DemoEffect.Navigate("toolkit/does-not-exist")
        routeDemoEffect(effect, navigator, forwarded::add)

        assertEquals(listOf<DemoEffect>(effect), forwarded)
        assertTrue(navigator.navigated.isEmpty(), "an unrecognized route key must not drive the navigator")
    }

    @Test
    fun toast_effect_is_forwarded_and_does_not_navigate() {
        val navigator = RecordingNavigator()
        val forwarded = mutableListOf<DemoEffect>()

        val toast = DemoEffect.Toast("hi")
        routeDemoEffect(toast, navigator, forwarded::add)

        assertEquals(listOf<DemoEffect>(toast), forwarded)
        assertTrue(navigator.navigated.isEmpty(), "toast effects must not drive the navigator")
    }
}

private class RecordingNavigator : FrnkNavigator {
    val navigated = mutableListOf<Any>()

    override fun navigate(route: Any) {
        navigated += route
    }

    override fun navigate(
        route: Any,
        options: FrnkNavOptions,
    ) {
        navigated += route
    }

    override fun navigateUp(): Boolean = false

    override fun popBackStack(): Boolean = false
}
