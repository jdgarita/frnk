package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * [frnkRootNavConfig] must be symmetric with [frnkNestedNavConfig]: it registers every [FrnkRootRoute]
 * member — including [FrnkRootRoute.Custom], which the old `val` form silently dropped — and merges the
 * host's own root routes via `hostRoutes`. nav3 serializes back-stack keys polymorphically over [NavKey],
 * so an unregistered route can't be persisted/restored; these assert the polymorphic registrations exist
 * (the only automated coverage for the root host-extensibility, since the demo opens the paywall via the
 * helper-free path).
 */
class RootNavConfigTest {
    @Serializable
    private data class HostRootRoute(
        val id: String
    ) : NavKey

    @Test
    fun registers_all_root_route_members_including_custom() {
        val module = frnkRootNavConfig().serializersModule
        assertNotNull(module.getPolymorphic(NavKey::class, FrnkRootRoute.Onboarding))
        assertNotNull(module.getPolymorphic(NavKey::class, FrnkRootRoute.Tab))
        assertNotNull(module.getPolymorphic(NavKey::class, FrnkRootRoute.Paywall))
        assertNotNull(
            module.getPolymorphic(NavKey::class, FrnkRootRoute.Custom("x")),
            "FrnkRootRoute.Custom must be registered for root host-extensibility"
        )
    }

    @Test
    fun merges_host_root_routes() {
        val hostRoutes =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(HostRootRoute::class, HostRootRoute.serializer())
                }
            }
        val module = frnkRootNavConfig(hostRoutes = hostRoutes).serializersModule
        assertNotNull(
            module.getPolymorphic(NavKey::class, HostRootRoute("home")),
            "host-supplied root routes must be merged via hostRoutes"
        )
    }
}