package dev.jdgarita.frnk.shared

import dev.jdgarita.frnk.backend.firebase.firebaseBackendModule
import dev.jdgarita.frnk.backend.supabase.supabaseBackendModule
import dev.jdgarita.frnk.monetization.monetizationModule
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatModule
import dev.jdgarita.frnk.monetization.ui.paywallScaffoldModule
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
