package dev.jdgarita.frnk.demo

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatModule
import org.koin.core.KoinApplication

/**
 * iOS real-RevenueCat demo entry point (BACKLOG P3-3) — parity with `androidDemoApp`.
 *
 * Configures RevenueCat with a **Test Store** `test_` API key (routes purchases to the Test Store, no
 * App Store Connect needed) and bootstraps the demo Koin graph with the **real** [revenueCatModule]
 * overriding the demo's [FakeEntitlementProvider]. The frnk Free/Pro layer + god mode (over the fake
 * `KeyValueStore`) are unchanged — only the `EntitlementProvider` swaps to the real one.
 *
 * Swift calls this instead of [bootstrapDemoKoin]. The native `purchases-ios` SDK must be supplied by
 * `iosDemoApp` via SPM (the `RevenueCat` package); DemoKit links those symbols under `dynamic_lookup`.
 */
fun bootstrapDemoKoinWithRevenueCat(apiKey: String): KoinApplication {
    Purchases.configure(apiKey)
    return bootstrapDemoKoin {
        allowOverride(true)
        modules(revenueCatModule)
    }
}
