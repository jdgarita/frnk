package dev.jdgarita.frnk.shared

import dev.jdgarita.frnk.backend.firebase.firebaseBackendModule
import dev.jdgarita.frnk.backend.firebase.firebaseObservabilityModule
import dev.jdgarita.frnk.database.impl.databaseModule
import dev.jdgarita.frnk.monetization.monetizationModule
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatModule
import dev.jdgarita.frnk.monetization.ui.paywallScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.bottomNavScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.homeScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.onboardingScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.settingsScaffoldModule
import org.koin.core.module.Module

/**
 * Assembles the toolkit's Koin modules for the chosen backend / observability / monetization axes,
 * plus any host-supplied [additionalModules].
 *
 * @param additionalModules host modules appended to the graph (repositories, feature ViewModels, a
 *   custom `EntitlementProvider`, a host `SqlDriver` schema, …). They install **after** the toolkit's,
 *   so a host can override a toolkit binding by enabling Koin override in `extraConfig`/`startKoin`.
 */
fun frnkModules(
    backend: BackendChoice = BackendChoice.Firebase,
    observability: ObservabilityChoice = ObservabilityChoice.None,
    monetization: MonetizationChoice = MonetizationChoice.RevenueCat,
    additionalModules: List<Module> = emptyList(),
): List<Module> =
    buildList {
        add(databaseModule)
        // Toolkit scaffold ViewModels — tiny SDK-free factories, installed unconditionally so the
        // VM-backed scaffolds (Home/Settings/Onboarding/BottomNav and the FrnkAppScaffold shell built
        // on them) resolve without each host remembering a per-scaffold `includes(...)`.
        add(homeScaffoldModule)
        add(settingsScaffoldModule)
        add(onboardingScaffoldModule)
        add(bottomNavScaffoldModule)
        add(
            when (backend) {
                BackendChoice.Firebase -> firebaseBackendModule
            },
        )
        add(
            when (observability) {
                ObservabilityChoice.None -> noopObservabilityModule
                ObservabilityChoice.Firebase -> firebaseObservabilityModule
            },
        )
        when (monetization) {
            // RevenueCat supplies the EntitlementProvider; monetizationModule binds the frnk-owned
            // EntitlementManager (god mode + Free/Pro layer) + FeatureGate over it; paywallScaffoldModule
            // registers the paywall VM.
            MonetizationChoice.RevenueCat -> {
                add(revenueCatModule)
                add(monetizationModule)
                add(paywallScaffoldModule)
            }
            // No monetization bindings — the host opts out or wires its own provider via additionalModules.
            MonetizationChoice.None -> Unit
        }
        addAll(additionalModules)
    }
