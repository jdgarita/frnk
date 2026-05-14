package dev.jdgarita.frnk.monetization.revenuecat

import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.FeatureGate
import org.koin.dsl.module

val revenueCatModule = module {
    single<EntitlementManager> { RevenueCatEntitlementManager() }
    single { FeatureGate(get(), get()) }
}
