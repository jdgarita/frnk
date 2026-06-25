package dev.jdgarita.frnk.ui.app

import dev.jdgarita.frnk.backend.noopObservabilityModule
import dev.jdgarita.frnk.monetization.monetizationModule
import dev.jdgarita.frnk.monetization.ui.paywallScaffoldModule
import dev.jdgarita.frnk.remoteconfig.noopRemoteConfigModule
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pure list-inspection of [frnkModules] (no `startKoin`): the slots default to the no-op modules, the
 * monetization trio is bundled from just the provider, and an assigned slot replaces the default. The
 * XOR guarantee itself is a compile-time property (a single `var` slot) — not testable at runtime.
 */
class FrnkModulesBuilderTest {
    private val customProviderModule = module { single { "fake-entitlement-provider" } }
    private val hostModule = module { single { "host-binding" } }

    @Test
    fun defaults_to_noop_observability_remote_config_and_scaffold_vms_without_monetization() {
        val modules = frnkModules { }
        frnkUiModules().forEach { assertTrue(it in modules, "scaffold VM module $it") }
        assertTrue(noopObservabilityModule in modules, "default observability is no-op")
        assertTrue(noopRemoteConfigModule in modules, "default remote-config is no-op")
        assertFalse(monetizationModule in modules, "no monetization unless a provider is set")
        assertFalse(paywallScaffoldModule in modules, "no paywall unless a provider is set")
    }

    @Test
    fun monetization_provider_auto_bundles_the_trio() {
        val modules =
            frnkModules {
                monetization(provider = customProviderModule)
            }
        assertTrue(customProviderModule in modules, "the host-supplied provider")
        assertTrue(monetizationModule in modules, "monetizationModule auto-added")
        assertTrue(paywallScaffoldModule in modules, "paywallScaffoldModule auto-added")
    }

    @Test
    fun assigned_slots_replace_the_defaults_and_extras_are_carried() {
        val customObservability = module { single { "custom-observability" } }
        val modules =
            frnkModules {
                observability = customObservability
                modules(hostModule)
            }
        assertTrue(customObservability in modules, "assigned observability slot")
        assertFalse(noopObservabilityModule in modules, "default observability replaced (XOR)")
        assertTrue(hostModule in modules, "host extras carried through")
    }
}