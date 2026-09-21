package dev.jdgarita.frnk.ui.app

import dev.jdgarita.frnk.backend.noopAnalyticsModule
import dev.jdgarita.frnk.backend.noopCrashReportingModule
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
 * - **XOR by construction.** [analytics], [crashReporting], [remoteConfig] and [identity] are single
 *   slots, so installing two bindings for one of them — the silent-shadowing footgun — is
 *   unrepresentable. Analytics and crash reporting are separate slots because they are separate
 *   vendors now: a host can ship PostHog with no crash reporter, Sentry with no analytics, or either
 *   with Firebase; identity is its own slot because RevenueCat monetization with Firebase identity is
 *   a legitimate pairing too.
 * - **No forgotten monetization trio.** [monetization] takes only the provider; [build] auto-adds
 *   `monetizationModule` + `paywallScaffoldModule` so the stack is always complete.
 * - **Scaffold VMs included.** [build] always prepends [frnkUiModules].
 *
 * ```kotlin
 * initializeFrnk(
 *     context = this,
 *     modules = frnkModules {
 *         analytics = postHogAnalyticsModule(…)         // host imports the provider's val
 *         crashReporting = sentryCrashReportingModule(…) // likewise
 *         remoteConfig = remoteConfigModule             // host imports the :remote-config-impl val
 *         monetization(provider = revenueCatModule)     // + monetizationModule + paywallScaffoldModule
 *         identity = revenueCatIdentityModule           // the app user id as the anonymous identity
 *         modules(databaseModule, prefsModule, *hostModules.toTypedArray())
 *     },
 *     validate = true,
 *     validator = Koin::validateFrnkBootstrap,
 * )
 * ```
 *
 * Defaults are the no-op modules ([noopAnalyticsModule] / [noopCrashReportingModule] /
 * [noopRemoteConfigModule]), so an `frnkModules { }` with no overrides yields a runnable
 * telemetry-free graph. [identity] has no no-op: a host without monetization needs none, and one
 * with it must choose (the validator says so if it forgets).
 */
class FrnkModulesScope internal constructor() {
    /** Product-analytics binding (`AnalyticsTracker`) — one slot enforces the provider XOR no-op rule. */
    var analytics: Module = noopAnalyticsModule

    /** Crash-reporting binding (`CrashReporter`) — one slot enforces the provider XOR no-op rule. */
    var crashReporting: Module = noopCrashReportingModule

    /** Remote-config binding — one slot enforces the provider XOR no-op rule. Defaults to no-op. */
    var remoteConfig: Module = noopRemoteConfigModule

    /**
     * Anonymous-identity binding (`AnonymousIdentityProvider`) — `revenueCatIdentityModule`
     * (`:monetization-impl`, the RevenueCat app user id) or `firebaseIdentityModule` (`:identity-impl`).
     * Required whenever [monetization] is set: its `SyncAuthUseCase` reads it. Unset by default.
     */
    var identity: Module? = null

    private var monetizationProvider: Module? = null
    private val extras = mutableListOf<Module>()

    /**
     * Installs the monetization stack over [provider] (the `EntitlementProvider` binding, e.g.
     * `revenueCatModule` from `:monetization-impl`). [build] bundles `monetizationModule` +
     * `paywallScaffoldModule` so the host can't forget either half. Pair it with an [identity].
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
            add(analytics)
            add(crashReporting)
            add(remoteConfig)
            identity?.let(::add)
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