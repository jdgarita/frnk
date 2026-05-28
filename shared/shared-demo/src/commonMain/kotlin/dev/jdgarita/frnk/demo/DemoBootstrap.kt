package dev.jdgarita.frnk.demo

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * Single entry point for both `androidDemoApp` and `iosDemoApp`. Installs only [demoModule],
 * which binds fakes for every `*-api` interface the screen touches (`FakeEntitlementManager`,
 * `LoggingAnalyticsTracker`, `LoggingCrashReporter`). No real backend or RevenueCat init —
 * by design — so the demo runs without `GoogleService-Info.plist`, without CocoaPods, and
 * without any native iOS SDK. Apps that want a real backend wire `frnkModules(BackendChoice)`
 * via `:shared` / `FrnkKit.xcframework` instead.
 */
fun bootstrapDemoKoin(extraConfig: KoinApplication.() -> Unit = {}): KoinApplication =
    startKoin {
        modules(demoModule)
        extraConfig()
    }
