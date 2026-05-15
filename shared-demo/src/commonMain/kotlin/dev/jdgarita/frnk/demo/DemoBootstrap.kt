package dev.jdgarita.frnk.demo

import dev.jdgarita.frnk.shared.BackendChoice
import dev.jdgarita.frnk.shared.frnkModules
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/**
 * Single entry point for both `androidDemoApp` and `iosDemoApp`. Starts Koin with the
 * toolkit's [frnkModules] for the chosen backend plus the demo's fake bindings.
 *
 * `frnkModules` registers the production database/backend/monetization impls; [demoModule]
 * binds [FakeEntitlementManager], [LoggingAnalyticsTracker], and [LoggingCrashReporter] AFTER
 * those — Koin's last-write-wins semantics for `single<…>` then makes the demo fakes the
 * resolved bindings, so the screen exercises FeatureGate without hitting RevenueCat.
 */
fun bootstrapDemoKoin(
    backend: BackendChoice = BackendChoice.Supabase,
    extraConfig: KoinApplication.() -> Unit = {},
): KoinApplication =
    startKoin {
        allowOverride(true)
        modules(frnkModules(backend) + demoModule)
        extraConfig()
    }

/** Stop the current Koin container and re-bootstrap with a different backend. */
fun swapBackend(newBackend: BackendChoice): KoinApplication {
    stopKoin()
    return bootstrapDemoKoin(newBackend)
}
