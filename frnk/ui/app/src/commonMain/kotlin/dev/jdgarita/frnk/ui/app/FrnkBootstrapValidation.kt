package dev.jdgarita.frnk.ui.app

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.usecase.ObserveProStatusUseCase
import dev.jdgarita.frnk.remoteconfig.RemoteConfigService
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.error.InstanceCreationException

/**
 * Fail-fast check that the started [Koin] graph holds the modules a frnk-UI host requires, turning an
 * incomplete `initializeFrnk(...)` list into one explained crash naming the missing module instead of a
 * deep `NoDefinitionFound` the first time a scaffold resolves a binding.
 *
 * Wire it via `initializeFrnk(modules = …, validate = true, validator = Koin::validateFrnkBootstrap)`,
 * or call [KoinApplication.checkFrnkModules] on the returned application.
 *
 * **Required** (missing ⇒ throw): one observability module (`AnalyticsTracker` + `CrashReporter`), one
 * remote-config module (`RemoteConfigService`), and the monetization stack (`ObserveProStatusUseCase` +
 * `EntitlementManager` — the always-installed Settings scaffold reads pro-status). **Optional** (never
 * checked): `KeyValueStore` / `SqlDriverFactory`, which a local-only host legitimately omits.
 *
 * This runs **after** `startKoin`, so it can only detect *missing* bindings — it cannot see a *duplicate*
 * observability install (two modules collapse to one binding). Preventing that double-install is
 * [frnkModules]'s job (its single-slot `observability`/`remoteConfig` make it unrepresentable).
 */
fun Koin.validateFrnkBootstrap() {
    val missing =
        buildList {
            if (!isBound<AnalyticsTracker>() || !isBound<CrashReporter>()) {
                add(
                    "observability — install firebaseObservabilityModule (:analytics-impl) " +
                        "or noopObservabilityModule (:analytics-api)"
                )
            }
            if (!isBound<RemoteConfigService>()) {
                add(
                    "remote config — install remoteConfigModule (:remote-config-impl) " +
                        "or noopRemoteConfigModule (:remote-config-api)"
                )
            }
            if (!isBound<ObserveProStatusUseCase>() || !isBound<EntitlementManager>()) {
                add(
                    "monetization — the Settings scaffold needs ObserveProStatusUseCase; install " +
                        "monetizationModule (:monetization-api) over an EntitlementProvider " +
                        "(e.g. revenueCatModule from :monetization-impl) + paywallScaffoldModule"
                )
            }
        }
    check(missing.isEmpty()) {
        buildString {
            append("frnk bootstrap is incomplete — initializeFrnk(...) is missing required bindings:")
            missing.forEach { append("\n  • ").append(it) }
            append("\nSee docs/HOST_INTEGRATION.md §4, or assemble the list with frnkModules { }.")
        }
    }
}

/** [validateFrnkBootstrap] over a [KoinApplication]'s [Koin] — call it on the `initializeFrnk(...)` result. */
fun KoinApplication.checkFrnkModules() = koin.validateFrnkBootstrap()

/**
 * Whether [T] has a binding the host installed. A definition that is present but fails to construct
 * because a *transitive* dep is missing ([InstanceCreationException]) counts as bound — the missing
 * leaf dep is reported by its own check, so we don't double-report (e.g. a missing `AnalyticsTracker`
 * surfaces as "observability", not as a misleading "monetization missing" when `monetizationModule`
 * is in fact installed). Only a truly absent definition (`getOrNull` returns `null`) is unbound.
 */
private inline fun <reified T : Any> Koin.isBound(): Boolean =
    try {
        getOrNull<T>() != null
    } catch (_: InstanceCreationException) {
        true
    }