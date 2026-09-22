package dev.jdgarita.frnk.monetization.revenuecat

import dev.jdgarita.frnk.identity.AnonymousIdentityProvider
import dev.jdgarita.frnk.monetization.EntitlementProvider
import org.koin.dsl.module

/**
 * RevenueCat binds **only** the [EntitlementProvider]. The frnk-owned `EntitlementManager` + `FeatureGate`
 * come from `monetizationModule` (`shared-monetization-api`), so god mode / the Free-Pro layer stay
 * independent of RevenueCat. Identity is a separate slot — see [revenueCatIdentityModule].
 */
val revenueCatModule =
    module {
        single { RevenueCatConfig() }
        single<EntitlementProvider> { RevenueCatEntitlementProvider(get()) }
    }

/**
 * The RevenueCat app user id as the app's [AnonymousIdentityProvider] — assign to
 * `frnkModules { identity = … }`. Kept apart from [revenueCatModule] so a host picks its identity the
 * way it picks every other axis, one binding per slot: RevenueCat monetization with a host-owned
 * identity is a legitimate combination, and two modules binding the same type would only shadow each
 * other silently.
 */
val revenueCatIdentityModule =
    module {
        single<AnonymousIdentityProvider> { RevenueCatIdentityProvider() }
    }