package dev.jdgarita.frnk.monetization.revenuecat

import dev.jdgarita.frnk.monetization.EntitlementProvider
import org.koin.dsl.module

/**
 * RevenueCat binds **only** the [EntitlementProvider]. The frnk-owned `EntitlementManager` + `FeatureGate`
 * come from `monetizationModule` (`shared-monetization-api`), so god mode / the Free-Pro layer stay
 * independent of RevenueCat.
 */
val revenueCatModule =
    module {
        single { RevenueCatConfig() }
        single<EntitlementProvider> { RevenueCatEntitlementProvider(get()) }
    }
