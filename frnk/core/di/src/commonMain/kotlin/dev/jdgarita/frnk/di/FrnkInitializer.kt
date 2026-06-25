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
 *             databaseModule, prefsModule,         // SQLDelight driver factory / KeyValueStore
 *             firebaseObservabilityModule,         // or noopObservabilityModule
 *             revenueCatModule, monetizationModule, paywallScaffoldModule, // monetization stack
 *         ) + hostModules,
 * )
 * ```
 *
 * @param validate when `true`, runs [validator] against the started [Koin] right after `startKoin`,
 *   turning an incomplete module list into an immediate, explained crash instead of a deep
 *   `NoDefinitionFound` later. Off by default (the explicit module list stays no-magic). The toolkit's
 *   rule set is `:ui-app`'s `Koin::validateFrnkBootstrap` — `:core-di` stays capability-agnostic and only
 *   runs whatever check it is handed.
 * @param validator the check invoked when [validate] is `true`. Defaults to a no-op so this module
 *   names no capability type; hosts on the `:ui-app` layer pass `validator = Koin::validateFrnkBootstrap`.
 * @param extraConfig escape hatch for anything else the host needs on the `KoinApplication`
 *   (`androidContext(...)`, `allowOverride(true)`, logging, …). Android hosts should prefer the
 *   androidMain `initializeFrnk(context, modules)` overload, which wires the context for them.
 */
fun initializeFrnk(
    modules: List<Module>,
    validate: Boolean = false,
    validator: (Koin) -> Unit = {},
    extraConfig: KoinApplication.() -> Unit = {}
): KoinApplication =
    startKoin {
        modules(modules)
        extraConfig()
    }.also { if (validate) validator(it.koin) }

/**
 * Fail-fast accessor for the global Koin started by [initializeFrnk] — used by entry-point
 * composables (e.g. `FrnkApp`) to turn a missing bootstrap into an immediate, explained
 * crash instead of a deep `NoDefinitionFound` later.
 */
fun requireFrnkKoin(): Koin =
    checkNotNull(KoinPlatformTools.defaultContext().getOrNull()) {
        "Koin is not started. Call initializeFrnk(...) before composing FrnkApp — " +
            "from Application.onCreate (initializeFrnk(context = this, modules = …)) on Android, " +
            "or before creating the ComposeUIViewController on iOS."
    }