package dev.jdgarita.frnk.ui.app

import dev.jdgarita.frnk.backend.posthog.PostHogAnalyticsConfig
import dev.jdgarita.frnk.backend.posthog.postHogAnalyticsModule
import dev.jdgarita.frnk.backend.sentry.SentryCrashReportingConfig
import dev.jdgarita.frnk.backend.sentry.sentryCrashReportingModule
import dev.jdgarita.frnk.monetization.monetizationModule
import dev.jdgarita.frnk.monetization.ui.paywallScaffoldModule
import org.koin.core.module.Module

/**
 * Typed assembler for the Koin module list passed to `initializeFrnk(...)`. A thin convenience over
 * hand-building the list — it does **not** replace the explicit `initializeFrnk(modules = …)` path.
 *
 * What it buys over a raw `listOf(...)`:
 * - **Observability is not optional and not swappable.** Every frnk host ships PostHog analytics and
 *   Sentry crash reporting — there is no no-op and no alternative provider — so [observability]
 *   installs the toolkit's own `postHogAnalyticsModule` / `sentryCrashReportingModule`. The host
 *   supplies only its **Sentry** config (each app has its own Sentry project and DSN); the PostHog
 *   config is derived from it, because every frnk app reports to the one toolkit-wide PostHog project
 *   whose key ships in `:analytics-posthog` (`FrnkPostHogProject`). A host cannot forget it ([build]
 *   refuses to assemble without it), cannot install two `AnalyticsTracker` bindings, and never has to
 *   import a provider module or hold a PostHog key. It is the one place the builder references an
 *   SDK-backed module (`:analytics-posthog` + `:crash-sentry` are `api` deps of `:ui-app`): every host
 *   links both native SDKs anyway, so nothing is dragged in that a host would not otherwise carry. The
 *   host reads the two trackers back through Koin (`koinInject<AnalyticsTracker>()` /
 *   `get<CrashReporter>()`) for its own events and non-fatals.
 * - **XOR by construction for what is still a choice.** [identity] is a single slot, so installing
 *   two identity bindings — the silent-shadowing footgun — is unrepresentable.
 * - **No forgotten monetization trio.** [monetization] takes only the provider; [build] auto-adds
 *   `monetizationModule` + `paywallScaffoldModule` so the stack is always complete.
 * - **Scaffold VMs included.** [build] always prepends [frnkUiModules].
 *
 * ```kotlin
 * initializeFrnk(
 *     context = this,
 *     modules = frnkModules {
 *         observability(sentry = SentryCrashReportingConfig(dsn = BuildConfig.SENTRY_DSN, environment = env))
 *         monetization(provider = revenueCatModule)     // + monetizationModule + paywallScaffoldModule
 *         identity = revenueCatIdentityModule           // the app user id as the anonymous identity
 *         modules(databaseModule, prefsModule, *hostModules.toTypedArray())
 *     },
 *     validate = true,
 *     validator = Koin::validateFrnkBootstrap,
 * )
 * ```
 *
 * [identity] has no default: a host without monetization needs none,
 * and one with it must choose (the validator says so if it forgets).
 */
class FrnkModulesScope internal constructor() {
    /**
     * Anonymous-identity binding (`AnonymousIdentityProvider`) — `revenueCatIdentityModule`
     * (`:monetization-impl`, the RevenueCat app user id), or a host-owned binding.
     * Required whenever [monetization] is set: its `SyncAuthUseCase` reads it. Unset by default.
     */
    var identity: Module? = null

    private var observabilityModules: List<Module>? = null
    private var monetizationProvider: Module? = null
    private val extras = mutableListOf<Module>()

    /**
     * Installs the toolkit's observability pair — Sentry as the `CrashReporter` from the host's
     * [sentry] config (each app has its own Sentry project and DSN), PostHog as the
     * `AnalyticsTracker` on the toolkit-wide project (its key ships in `:analytics-posthog`), with
     * [postHog] defaulting to a config that mirrors [sentry]'s `environment`. Pass [postHog] only to
     * tune a flag (`debug`, `optOut`, …). Mandatory: [build] fails without it. The Sentry config
     * rejects a blank DSN at construction, so a clone without keys fails here with a message naming
     * the missing value, never with silently dropped telemetry.
     */
    fun observability(
        sentry: SentryCrashReportingConfig,
        postHog: PostHogAnalyticsConfig = PostHogAnalyticsConfig(environment = sentry.environment)
    ) {
        observabilityModules = listOf(postHogAnalyticsModule(postHog), sentryCrashReportingModule(sentry))
    }

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

    internal fun build(): List<Module> {
        val observability =
            checkNotNull(observabilityModules) {
                "frnkModules { } is missing observability(sentry = …) — every frnk host ships Sentry crash " +
                    "reporting + PostHog analytics; pass your SentryCrashReportingConfig (see docs/HOST_INTEGRATION.md §4)"
            }
        return buildList {
            addAll(frnkUiModules())
            addAll(observability)
            identity?.let(::add)
            monetizationProvider?.let {
                add(it)
                add(monetizationModule)
                add(paywallScaffoldModule)
            }
            addAll(extras)
        }
    }
}

/**
 * Builds the `initializeFrnk(...)` module list from a [FrnkModulesScope]. See [FrnkModulesScope] for
 * the slots; [FrnkModulesScope.observability] is mandatory.
 */
fun frnkModules(block: FrnkModulesScope.() -> Unit): List<Module> = FrnkModulesScope().apply(block).build()