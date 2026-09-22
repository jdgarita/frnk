package dev.jdgarita.frnk.demo

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import dev.jdgarita.frnk.backend.posthog.PostHogAnalyticsConfig
import dev.jdgarita.frnk.backend.sentry.SentryCrashReportingConfig
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatIdentityModule
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatModule
import org.koin.core.KoinApplication
import kotlin.experimental.ExperimentalNativeApi

/**
 * iOS real-SDK demo entry point — the parity partner of `DemoApplication` on Android. Sentry and
 * PostHog are mandatory (blank keys fail inside the configs, naming the missing one); RevenueCat's
 * `revenueCatModule` + `revenueCatIdentityModule` override the demo's fake provider / identity.
 *
 * The native SDKs (SPM products `RevenueCat`, `Sentry`, `PostHog`) must be linked by `iosDemoApp`;
 * DemoKit defers their symbols under `dynamic_lookup`. `Sentry.init` installs the SDK's own
 * unhandled-Kotlin-exception hook, so the "Force crash" panic button reports to Sentry with no
 * extra native wiring.
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
    return bootstrapDemoKoin(
        postHog =
            PostHogAnalyticsConfig(
                apiKey = postHogApiKey,
                host = postHogHost.ifBlank { PostHogAnalyticsConfig.DEFAULT_HOST },
                environment = environment
            ),
        sentry = SentryCrashReportingConfig(dsn = sentryDsn, environment = environment)
    ) {
        allowOverride(true)
        modules(revenueCatModule, revenueCatIdentityModule)
    }
}