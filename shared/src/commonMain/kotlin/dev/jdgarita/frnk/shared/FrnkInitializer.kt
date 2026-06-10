package dev.jdgarita.frnk.shared

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 * One-shot Koin bootstrap for host apps. Installs [frnkModules] for the chosen axes plus any
 * [additionalModules], then runs [extraConfig] for host-specific `startKoin` wiring.
 *
 * @param additionalModules first-class hook for a host's own Koin modules (repositories, feature
 *   ViewModels, a custom `EntitlementProvider`, …). Equivalent to calling `modules(...)` inside
 *   [extraConfig], but discoverable from the signature.
 * @param extraConfig escape hatch for anything else the host needs on the `KoinApplication`
 *   (`androidContext(...)`, `allowOverride(true)`, logging, …).
 */
fun initializeFrnk(
    backend: BackendChoice = BackendChoice.Firebase,
    observability: ObservabilityChoice = ObservabilityChoice.None,
    monetization: MonetizationChoice = MonetizationChoice.RevenueCat,
    additionalModules: List<Module> = emptyList(),
    extraConfig: KoinApplication.() -> Unit = {},
): KoinApplication =
    startKoin {
        modules(frnkModules(backend, observability, monetization, additionalModules))
        extraConfig()
    }
