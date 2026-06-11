package dev.jdgarita.frnk.ui.app

import dev.jdgarita.frnk.ui.scaffolds.bottomNavScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.homeScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.onboardingScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.settingsScaffoldModule
import org.koin.core.module.Module

/**
 * The toolkit's SDK-free scaffold ViewModel modules — tiny factories every VM-backed scaffold
 * (Home/Settings/Onboarding/BottomNav, and the [FrnkAppScaffold] shell built on them) resolves from.
 *
 * Hosts prepend this to their explicit Koin module list:
 *
 * ```kotlin
 * initializeFrnk(
 *     context = this, // Android; iOS omits it
 *     modules = frnkUiModules() +
 *         listOf(
 *             databaseModule,                      // SQLDelight driver factory + KeyValueStore
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
        homeScaffoldModule,
        settingsScaffoldModule,
        onboardingScaffoldModule,
        bottomNavScaffoldModule,
    )
