package dev.jdgarita.frnk.demo

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * Single entry point for both `androidDemoApp` and `iosDemoApp`. Installs only [demoModule],
 * which binds fakes for every `*-api` interface the screen touches (`FakeEntitlementManager`,
 * `LoggingAnalyticsTracker`, `LoggingCrashReporter`). No real backend or RevenueCat init — by design.
 *
 * Note: `iosDemoApp` additionally configures Firebase + installs the CrashKiOS hook
 * ([enableDemoCrashlytics]) **outside** this function so its "Force crash" panic button reports to
 * Crashlytics (BACKLOG P1-5b); that's the only real native SDK the demo touches. Apps that want a
 * real backend wire `frnkModules(BackendChoice)` via `:shared` / `FrnkKit.xcframework` instead.
 */
fun bootstrapDemoKoin(extraConfig: KoinApplication.() -> Unit = {}): KoinApplication =
    startKoin {
        modules(demoModule)
        extraConfig()
    }
