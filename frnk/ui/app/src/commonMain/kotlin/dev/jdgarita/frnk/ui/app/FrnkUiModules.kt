package dev.jdgarita.frnk.ui.app

import dev.jdgarita.frnk.ui.bottomnav.frnkNestedNavModule
import dev.jdgarita.frnk.ui.scaffolds.home.homeScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.onboarding.onboardingScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.settings.settingsScaffoldModule
import org.koin.core.module.Module

/**
 * The toolkit's SDK-free scaffold ViewModel modules — tiny factories every VM-backed scaffold
 * (Home/Settings/Onboarding) resolves from.
 *
 * Hosts prepend this to their explicit Koin module list:
 *
 * ```kotlin
 * initializeFrnk(
 *     context = this, // Android; iOS omits it
 *     modules = frnkUiModules() +
 *         listOf(
 *             databaseModule, prefsModule,         // SQLDelight driver factory / KeyValueStore
 *             firebaseObservabilityModule,         // or noopObservabilityModule
 *             revenueCatModule, monetizationModule, paywallScaffoldModule, // monetization stack
 *         ) + hostModules,
 * )
 * ```
 *
 * Only the scaffold VMs live here — anything touching a third-party SDK (database, observability,
 * monetization) is a separate module the host installs explicitly.
 */
fun frnkUiModules(): List<Module> =
    listOf(
        appearanceModule,
        homeScaffoldModule,
        settingsScaffoldModule,
        onboardingScaffoldModule,
        frnkNestedNavModule
    )