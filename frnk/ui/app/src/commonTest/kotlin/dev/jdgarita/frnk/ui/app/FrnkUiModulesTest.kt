package dev.jdgarita.frnk.ui.app

import dev.jdgarita.frnk.ui.scaffolds.homeScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.onboardingScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.settingsScaffoldModule
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Verifies [frnkUiModules] carries every scaffold VM module `FrnkAppScaffold`/`FrnkAppShell` resolve
 * — pure list inspection, no `startKoin`. Guards the host bootstrap contract: a missing entry here
 * surfaces as a runtime `NoDefinitionFound` in every host.
 */
class FrnkUiModulesTest {
    @Test
    fun contains_every_scaffold_view_model_module() {
        val modules = frnkUiModules()
        assertTrue(homeScaffoldModule in modules, "home scaffold VM module")
        assertTrue(settingsScaffoldModule in modules, "settings scaffold VM module")
        assertTrue(onboardingScaffoldModule in modules, "onboarding scaffold VM module")
    }
}
