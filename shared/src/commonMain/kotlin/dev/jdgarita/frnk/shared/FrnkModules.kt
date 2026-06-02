package dev.jdgarita.frnk.shared

import dev.jdgarita.frnk.backend.firebase.firebaseBackendModule
import dev.jdgarita.frnk.backend.firebase.firebaseObservabilityModule
import dev.jdgarita.frnk.backend.supabase.supabaseBackendModule
import dev.jdgarita.frnk.database.impl.databaseModule
import dev.jdgarita.frnk.monetization.monetizationModule
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatModule
import dev.jdgarita.frnk.monetization.ui.paywallScaffoldModule
import org.koin.core.module.Module

fun frnkModules(
    backend: BackendChoice = BackendChoice.Supabase,
    observability: ObservabilityChoice = ObservabilityChoice.None,
): List<Module> =
    buildList {
        add(databaseModule)
        add(
            when (backend) {
                BackendChoice.Supabase -> supabaseBackendModule
                BackendChoice.Firebase -> firebaseBackendModule
            },
        )
        add(
            when (observability) {
                ObservabilityChoice.None -> noopObservabilityModule
                ObservabilityChoice.Firebase -> firebaseObservabilityModule
            },
        )
        // RevenueCat supplies the EntitlementProvider; monetizationModule binds the frnk-owned
        // EntitlementManager (god mode + Free/Pro layer) + FeatureGate over it.
        add(revenueCatModule)
        add(monetizationModule)
        add(paywallScaffoldModule)
    }
