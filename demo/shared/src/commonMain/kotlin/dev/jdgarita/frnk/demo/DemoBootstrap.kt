package dev.jdgarita.frnk.demo

import dev.jdgarita.frnk.ui.app.checkFrnkModules
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * Single entry point for both `androidDemoApp` and `iosDemoApp`. Installs only [frnkAppModule],
 * which binds fakes for every `*-api` interface the screen touches (`FakeEntitlementProvider`,
 * `FakeKeyValueStore`, `LoggingAnalyticsTracker`, `LoggingCrashReporter`). No real backend by design.
 *
 * The device demos override selected bindings with real SDKs to smoke-test them (Koin
 * `allowOverride(true)`): `androidDemoApp` assembles its override list with `frnkModules { }` and installs
 * `firebaseObservabilityModule` + `remoteConfigModule` + (when a key is set) `revenueCatModule`;
 * `iosDemoApp` calls [bootstrapDemoKoinWithRevenueCat] for the real RevenueCat Test Store path and
 * configures Firebase + the CrashKiOS hook ([enableDemoCrashlytics]) in Swift (BACKLOG P1-5b/P3-3).
 * Apps that want real backends pass an explicit toolkit-module list to `initializeFrnk(...)` (`:core-di`)
 * instead — see docs/HOST_INTEGRATION.md.
 *
 * Demonstrates Tier 2.2's fail-fast validation: [checkFrnkModules] runs on the fully-assembled graph
 * (overrides included, since [extraConfig] runs inside the `startKoin` block) before returning, so a
 * missing required module surfaces as one explained crash here rather than a deep `NoDefinitionFound`.
 */
fun bootstrapDemoKoin(extraConfig: KoinApplication.() -> Unit = {}): KoinApplication =
    startKoin {
        modules(frnkAppModule)
        extraConfig()
    }.apply { checkFrnkModules() }