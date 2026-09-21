package dev.jdgarita.frnk.ui.app

import dev.jdgarita.frnk.backend.noopAnalyticsModule
import dev.jdgarita.frnk.backend.noopCrashReportingModule
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
    fun defaults_to_noop_analytics_crash_remote_config_and_scaffold_vms_without_monetization() {
        val modules = frnkModules { }
        frnkUiModules().forEach { assertTrue(it in modules, "scaffold VM module $it") }
        assertTrue(noopAnalyticsModule in modules, "default analytics is no-op")
        assertTrue(noopCrashReportingModule in modules, "default crash reporting is no-op")
        assertTrue(noopRemoteConfigModule in modules, "default remote-config is no-op")
        assertFalse(monetizationModule in modules, "no monetization unless a provider is set")
        assertFalse(paywallScaffoldModule in modules, "no paywall unless a provider is set")
    }

    @Test
    fun identity_slot_is_unset_by_default_and_carried_when_assigned() {
        val identityModule = module { single { "fake-identity" } }
        assertFalse(identityModule in frnkModules { }, "no identity unless assigned")
        assertTrue(identityModule in frnkModules { identity = identityModule }, "assigned identity slot")
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
        val customAnalytics = module { single { "custom-analytics" } }
        val modules =
            frnkModules {
                analytics = customAnalytics
                modules(hostModule)
            }
        assertTrue(customAnalytics in modules, "assigned analytics slot")
        assertFalse(noopAnalyticsModule in modules, "default analytics replaced (XOR)")
        assertTrue(noopCrashReportingModule in modules, "the other slot keeps its default — the two are independent")
        assertTrue(hostModule in modules, "host extras carried through")
    }
}