package dev.jdgarita.frnk.shared

import dev.jdgarita.frnk.backend.firebase.firebaseBackendModule
import dev.jdgarita.frnk.backend.supabase.supabaseBackendModule
import dev.jdgarita.frnk.monetization.monetizationModule
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatModule
import dev.jdgarita.frnk.monetization.ui.paywallScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.bottomNavScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.homeScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.onboardingScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.settingsScaffoldModule
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies [frnkModules] assembles the right Koin module set per axis — pure list inspection, no
 * `startKoin` (so no platform SDK / Android context needed). Guards the backend / monetization
 * switches and the host `additionalModules` hook against regressions.
 */
class FrnkModulesTest {
    @Test
    fun defaults_install_supabase_and_revenuecat_stack() {
        val modules = frnkModules()
        assertTrue(supabaseBackendModule in modules, "default backend is Supabase")
        assertFalse(firebaseBackendModule in modules, "Firebase backend not installed by default")
        assertTrue(revenueCatModule in modules, "RevenueCat is the default monetization provider")
        assertTrue(monetizationModule in modules)
        assertTrue(paywallScaffoldModule in modules)
    }

    @Test
    fun scaffold_view_models_install_for_every_axis_combination() {
        val combinations =
            BackendChoice.entries.flatMap { backend ->
                ObservabilityChoice.entries.flatMap { observability ->
                    MonetizationChoice.entries.map { monetization ->
                        frnkModules(backend, observability, monetization)
                    }
                }
            }
        combinations.forEach { modules ->
            assertTrue(homeScaffoldModule in modules, "home scaffold VM is always installed")
            assertTrue(settingsScaffoldModule in modules, "settings scaffold VM is always installed")
            assertTrue(onboardingScaffoldModule in modules, "onboarding scaffold VM is always installed")
            assertTrue(bottomNavScaffoldModule in modules, "bottom-nav scaffold VM is always installed")
        }
    }

    @Test
    fun firebase_backend_swaps_out_supabase() {
        val modules = frnkModules(backend = BackendChoice.Firebase)
        assertTrue(firebaseBackendModule in modules)
        assertFalse(supabaseBackendModule in modules, "unchosen backend never installed")
    }

    @Test
    fun monetization_none_omits_all_monetization_modules() {
        val modules = frnkModules(monetization = MonetizationChoice.None)
        assertFalse(revenueCatModule in modules)
        assertFalse(monetizationModule in modules)
        assertFalse(paywallScaffoldModule in modules)
    }

    @Test
    fun additional_modules_are_appended() {
        val hostModule: Module = module {}
        val modules = frnkModules(additionalModules = listOf(hostModule))
        assertTrue(hostModule in modules, "host modules are added to the graph")
        assertEquals(modules.last(), hostModule, "host modules install after the toolkit's")
    }
}
