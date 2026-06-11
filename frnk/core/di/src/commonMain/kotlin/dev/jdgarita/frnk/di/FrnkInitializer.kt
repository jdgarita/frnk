package dev.jdgarita.frnk.di

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.mp.KoinPlatformTools

/**
 * One-shot Koin bootstrap for host apps. The host passes the **explicit** list of toolkit + host
 * Koin modules it wants in the graph — there is no backend/observability/monetization switch;
 * unwanted modules are simply not passed:
 *
 * ```kotlin
 * initializeFrnk(
 *     modules = frnkUiModules() +                  // :ui-app — scaffold VM factories (Home/Settings/…)
 *         listOf(
 *             databaseModule,                      // SQLDelight driver factory + KeyValueStore
 *             firebaseObservabilityModule,         // or noopObservabilityModule
 *             revenueCatModule, monetizationModule, paywallScaffoldModule, // monetization stack
 *         ) + hostModules,
 * )
 * ```
 *
 * @param extraConfig escape hatch for anything else the host needs on the `KoinApplication`
 *   (`androidContext(...)`, `allowOverride(true)`, logging, …). Android hosts should prefer the
 *   androidMain `initializeFrnk(context, modules)` overload, which wires the context for them.
 */
fun initializeFrnk(
    modules: List<Module>,
    extraConfig: KoinApplication.() -> Unit = {},
): KoinApplication =
    startKoin {
        modules(modules)
        extraConfig()
    }

/**
 * Fail-fast accessor for the global Koin started by [initializeFrnk] — used by entry-point
 * composables (e.g. `FrnkAppScaffold`) to turn a missing bootstrap into an immediate, explained
 * crash instead of a deep `NoDefinitionFound` later.
 */
fun requireFrnkKoin(): Koin =
    checkNotNull(KoinPlatformTools.defaultContext().getOrNull()) {
        "Koin is not started. Call initializeFrnk(...) before composing FrnkAppScaffold — " +
            "from Application.onCreate (initializeFrnk(context = this, modules = …)) on Android, " +
            "or before creating the ComposeUIViewController on iOS."
    }
