package dev.jdgarita.frnk.demo

import dev.jdgarita.frnk.backend.posthog.PostHogAnalyticsConfig
import dev.jdgarita.frnk.backend.sentry.SentryCrashReportingConfig
import dev.jdgarita.frnk.ui.app.checkFrnkModules
import dev.jdgarita.frnk.ui.app.frnkModules
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * Single entry point for both `demo-android` and `iosDemoApp`. Assembles the graph the way a real
 * host does — `frnkModules { }` with the mandatory observability pair from the [postHog] / [sentry]
 * configs the platform read from its build config — plus [frnkAppModule], which binds fakes for the
 * paid-SDK seams the screen touches (`FakeEntitlementProvider`, `FakeKeyValueStore`, `FakeNoteStore`).
 *
 * The demo is treated as a real app: PostHog + Sentry are real, keys are required (a blank one fails
 * inside the config with a message naming it), and nothing in the toolkit exists to support a
 * key-less demo. Both device demos then override selected fakes with real SDKs (Koin
 * `allowOverride(true)` in [extraConfig]): `demo-android` installs `remoteConfigModule` + the Room
 * path + (when a key is set) `revenueCatModule`/`revenueCatIdentityModule`; `iosDemoApp` calls
 * `bootstrapDemoKoinWithSdks` for the RevenueCat Test Store path.
 *
 * Demonstrates Tier 2.2's fail-fast validation: [checkFrnkModules] runs on the fully-assembled graph
 * (overrides included, since [extraConfig] runs inside the `startKoin` block) before returning, so a
 * missing required module surfaces as one explained crash here rather than a deep `NoDefinitionFound`.
 */
fun bootstrapDemoKoin(
    postHog: PostHogAnalyticsConfig,
    sentry: SentryCrashReportingConfig,
    extraConfig: KoinApplication.() -> Unit = {}
): KoinApplication =
    startKoin {
        modules(
            frnkModules {
                observability(postHog = postHog, sentry = sentry)
                modules(frnkAppModule)
            }
        )
        extraConfig()
    }.apply { checkFrnkModules() }