package dev.jdgarita.frnk.monetization.revenuecat

import dev.jdgarita.frnk.identity.AnonymousIdentityProvider
import dev.jdgarita.frnk.monetization.EntitlementProvider
import org.koin.dsl.module

/**
 * RevenueCat binds the [EntitlementProvider] and, since the app user id *is* the app's anonymous
 * identity, the [AnonymousIdentityProvider] over it — so `monetization(provider = revenueCatModule)`
 * is the whole identity story for an accountless host. The frnk-owned `EntitlementManager` +
 * `FeatureGate` come from `monetizationModule` (`shared-monetization-api`), so god mode / the
 * Free-Pro layer stay independent of RevenueCat.
 */
val revenueCatModule =
    module {
        single { RevenueCatConfig() }
        single<EntitlementProvider> { RevenueCatEntitlementProvider(get()) }
        single<AnonymousIdentityProvider> { RevenueCatIdentityProvider() }
    }