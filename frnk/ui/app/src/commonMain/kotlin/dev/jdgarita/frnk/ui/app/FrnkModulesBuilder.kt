package dev.jdgarita.frnk.ui.app

import dev.jdgarita.frnk.backend.noopObservabilityModule
import dev.jdgarita.frnk.monetization.monetizationModule
import dev.jdgarita.frnk.monetization.ui.paywallScaffoldModule
import dev.jdgarita.frnk.remoteconfig.noopRemoteConfigModule
import org.koin.core.module.Module

/**
 * Typed assembler for the Koin module list passed to `initializeFrnk(...)`. A thin, additive
 * convenience over hand-building the list — it does **not** replace the explicit
 * `initializeFrnk(modules = …)` path, and it never references an `*-impl` module (the host imports
 * the impl `val` it wants and assigns it to a slot, so the toolkit stays cinterop-clean).
 *
 * What it buys over a raw `listOf(...)`:
 * - **XOR by construction.** [observability] and [remoteConfig] are single slots, so installing two
 *   observability (or two remote-config) modules — the silent-shadowing footgun — is unrepresentable.
 * - **No forgotten monetization trio.** [monetization] takes only the provider; [build] auto-adds
 *   `monetizationModule` + `paywallScaffoldModule` so the stack is always complete.
 * - **Scaffold VMs included.** [build] always prepends [frnkUiModules].
 *
 * ```kotlin
 * initializeFrnk(
 *     context = this,
 *     modules = frnkModules {
 *         observability = firebaseObservabilityModule   // host imports the :analytics-impl val
 *         remoteConfig = remoteConfigModule             // host imports the :remote-config-impl val
 *         monetization(provider = revenueCatModule)     // + monetizationModule + paywallScaffoldModule
 *         modules(databaseModule, prefsModule, *hostModules.toTypedArray())
 *     },
 *     validate = true,
 *     validator = Koin::validateFrnkBootstrap,
 * )
 * ```
 *
 * Defaults are the no-op modules ([noopObservabilityModule]/[noopRemoteConfigModule]), so an
 * `frnkModules { }` with no overrides yields a runnable telemetry-free graph.
 */
class FrnkModulesScope internal constructor() {
    /** Observability binding — one slot enforces the `firebase` XOR `noop` rule. Defaults to no-op. */
    var observability: Module = noopObservabilityModule

    /** Remote-config binding — one slot enforces the `firebase` XOR `noop` rule. Defaults to no-op. */
    var remoteConfig: Module = noopRemoteConfigModule

    private var monetizationProvider: Module? = null
    private val extras = mutableListOf<Module>()

    /**
     * Installs the monetization stack over [provider] (the `EntitlementProvider` binding, e.g.
     * `revenueCatModule` from `:monetization-impl`). [build] bundles `monetizationModule` +
     * `paywallScaffoldModule` so the host can't forget either half.
     */
    fun monetization(provider: Module) {
        monetizationProvider = provider
    }

    /** Adds host/prefs/database modules the host imports directly (e.g. `databaseModule`, `prefsModule`). */
    fun modules(vararg modules: Module) {
        extras += modules
    }

    /** Single-module convenience for [modules]. */
    fun module(module: Module) {
        extras += module
    }

    internal fun build(): List<Module> =
        buildList {
            addAll(frnkUiModules())
            add(observability)
            add(remoteConfig)
            monetizationProvider?.let {
                add(it)
                add(monetizationModule)
                add(paywallScaffoldModule)
            }
            addAll(extras)
        }
}

/**
 * Builds the `initializeFrnk(...)` module list from a [FrnkModulesScope]. See [FrnkModulesScope] for
 * the slots and the cinterop rule (assign impl `val`s the host imports; never reference them here).
 */
fun frnkModules(block: FrnkModulesScope.() -> Unit): List<Module> = FrnkModulesScope().apply(block).build()