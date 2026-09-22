package dev.jdgarita.frnk.demo

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import dev.jdgarita.frnk.backend.sentry.SentryCrashReportingConfig
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatIdentityModule
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatModule
import org.koin.core.KoinApplication
import platform.Foundation.NSBundle
import kotlin.experimental.ExperimentalNativeApi

/**
 * iOS real-SDK demo entry point — the parity partner of `DemoApplication` on Android. Reads the
 * keys from the app bundle's Info.plist, which forwards them from `Configuration/Config.xcconfig`
 * → the gitignored `Secrets.xcconfig` (same approach as Faint): `SENTRY_DSN` (the demo's own Sentry
 * project) and `REVENUECAT_API_KEY`. PostHog needs no key — every frnk app reports to the toolkit-wide
 * project shipped in `:analytics-posthog`. Sentry is mandatory: a missing `Secrets.xcconfig` leaves
 * the DSN blank and the config fails here, naming the key. RevenueCat's `revenueCatModule` +
 * `revenueCatIdentityModule` override the demo's fake provider / identity.
 *
 * The native SDKs (SPM products `RevenueCat`, `Sentry`, `PostHog`) must be linked by `iosDemoApp`;
 * DemoKit defers their symbols under `dynamic_lookup`. `Sentry.init` installs the SDK's own
 * unhandled-Kotlin-exception hook, so the "Force crash" panic button reports to Sentry with no
 * extra native wiring.
 */
@OptIn(ExperimentalNativeApi::class)
fun bootstrapDemoKoinWithSdks(): KoinApplication {
    Purchases.configure(infoPlistValue("REVENUECAT_API_KEY"))
    val environment = if (kotlin.native.Platform.isDebugBinary) "debug" else "release"
    return bootstrapDemoKoin(sentry = SentryCrashReportingConfig(dsn = infoPlistValue("SENTRY_DSN"), environment = environment)) {
        allowOverride(true)
        modules(revenueCatModule, revenueCatIdentityModule)
    }
}

/**
 * An Info.plist string forwarded from the xcconfig. An unexpanded `$(KEY)` (the xcconfig variable
 * was never defined because `Secrets.xcconfig` is missing) counts as blank, so the toolkit's
 * config reports the missing key instead of the SDK choking on a literal `$(…)`.
 */
private fun infoPlistValue(key: String): String {
    val raw = (NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? String).orEmpty().trim()
    return if (raw.startsWith("$(")) "" else raw
}