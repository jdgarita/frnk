package dev.jdgarita.frnk.monetization

import org.koin.dsl.module

/**
 * frnk-owned monetization bindings, independent of any billing SDK. Binds the canonical
 * [EntitlementManager] over whatever [EntitlementProvider] the host installs (RevenueCat in `:shared`,
 * a fake in `:shared-demo`) plus [FeatureGate].
 *
 * Requires an [EntitlementProvider], a [dev.jdgarita.frnk.database.KeyValueStore] (for god-mode
 * persistence), and an [dev.jdgarita.frnk.backend.AnalyticsTracker] to be present in the graph.
 */
val monetizationModule =
    module {
        single<EntitlementManager> { DefaultEntitlementManager(get(), get(), get()) }
        single { FeatureGate(get(), get()) }
    }
