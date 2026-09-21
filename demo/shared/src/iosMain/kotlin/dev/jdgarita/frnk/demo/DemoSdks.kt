package dev.jdgarita.frnk.demo

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import dev.jdgarita.frnk.backend.posthog.PostHogAnalyticsConfig
import dev.jdgarita.frnk.backend.posthog.postHogAnalyticsModule
import dev.jdgarita.frnk.backend.sentry.SentryCrashReportingConfig
import dev.jdgarita.frnk.backend.sentry.sentryCrashReportingModule
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatIdentityModule
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatModule
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import kotlin.experimental.ExperimentalNativeApi

/**
 * iOS real-SDK demo entry point — RevenueCat plus, when a key is passed, Sentry and PostHog. The
 * parity partner of `DemoApplication` on Android: the same `revenueCatModule` override as
 * [bootstrapDemoKoinWithRevenueCat], plus the toolkit's `sentryCrashReportingModule` /
 * `postHogAnalyticsModule` over the demo's logging fakes, and `revenueCatIdentityModule` over its fake
 * identity. A blank key leaves that slot on the fake.
 *
 * The native SDKs (SPM products `RevenueCat`, `Sentry`, `PostHog`) must be linked by `iosDemoApp`;
 * DemoKit defers their symbols under `dynamic_lookup`. When [sentryDsn] is set, do **not** also
 * install the CrashKiOS hook ([enableDemoCrashlytics]): `Sentry.init` installs its own
 * unhandled-Kotlin-exception hook, and two hooks double-report.
 */
@OptIn(ExperimentalNativeApi::class)
fun bootstrapDemoKoinWithSdks(
    revenueCatApiKey: String,
    sentryDsn: String,
    postHogApiKey: String,
    postHogHost: String
): KoinApplication {
    Purchases.configure(revenueCatApiKey)
    val environment = if (kotlin.native.Platform.isDebugBinary) "debug" else "release"
    val overrides =
        buildList<Module> {
            add(revenueCatModule)
            add(revenueCatIdentityModule)
            if (sentryDsn.isNotBlank()) {
                add(sentryCrashReportingModule(SentryCrashReportingConfig(dsn = sentryDsn, environment = environment)))
            }
            if (postHogApiKey.isNotBlank()) {
                add(
                    postHogAnalyticsModule(
                        PostHogAnalyticsConfig(
                            apiKey = postHogApiKey,
                            host = postHogHost.ifBlank { PostHogAnalyticsConfig.DEFAULT_HOST },
                            environment = environment
                        )
                    )
                )
            }
        }
    return bootstrapDemoKoin {
        allowOverride(true)
        modules(overrides)
    }
}