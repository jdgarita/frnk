package dev.jdgarita.frnk.demo

import android.app.Application
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import dev.jdgarita.frnk.backend.posthog.PostHogAnalyticsConfig
import dev.jdgarita.frnk.backend.sentry.SentryCrashReportingConfig
import dev.jdgarita.frnk.database.impl.databaseModule
import dev.jdgarita.frnk.demo.notes.demoNotesModule
import dev.jdgarita.frnk.di.DatabaseContext
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatIdentityModule
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatModule
import dev.jdgarita.frnk.remoteconfig.firebase.remoteConfigModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // The real Room path resolves the Application context through this seam; hosts on
        // initializeFrnk(context, …) get it set automatically, but the demo boots bootstrapDemoKoin().
        DatabaseContext.application = this
        // Observability is real on every host, the demo included: POSTHOG_API_KEY / SENTRY_DSN come
        // from local.properties (gitignored) via BuildConfig, and a blank one fails inside its config
        // with a message naming it — there is no logging fake or no-op to fall back to.
        val environment = if (BuildConfig.DEBUG) "debug" else "release"
        val postHog =
            PostHogAnalyticsConfig(
                apiKey = BuildConfig.POSTHOG_API_KEY,
                host = BuildConfig.POSTHOG_HOST.ifBlank { PostHogAnalyticsConfig.DEFAULT_HOST },
                environment = environment,
                debug = BuildConfig.DEBUG
            )
        val sentry =
            SentryCrashReportingConfig(
                dsn = BuildConfig.SENTRY_DSN,
                environment = environment,
                release = "${BuildConfig.APPLICATION_ID}@${BuildConfig.VERSION_NAME}+${BuildConfig.VERSION_CODE}",
                debug = BuildConfig.DEBUG
            )
        // The demo's fakes cover the paid-SDK seams; on Android we override selected ones with the
        // REAL toolkit modules to smoke-test the SDKs on a device:
        //  - databaseModule + demoNotesModule — the real Room path: the toolkit's DatabaseFactory
        //    (:data-db-impl) opening the demo-owned DemoDatabase, replacing the in-memory FakeNoteStore
        //    (restructure Stage 4 / OQ-2).
        //  - remoteConfigModule — real Firebase Remote Config (restructure Stage 11), replacing the
        //    no-op default so the demo's "Capabilities" section shows a live fetched value when a
        //    `demo_welcome_message` parameter is set in the frnk-demo Firebase project (else the
        //    bundled default). google-services.json + the google-services plugin already init Firebase.
        //  - revenueCatModule + revenueCatIdentityModule — real RevenueCat EntitlementManager
        //    (BACKLOG P3-2) + the app user id as the identity, installed only when a public Android
        //    SDK key is present in local.properties. Purchases.configure(...) must run before the
        //    override so the manager reads a configured SDK; the Android context is captured
        //    automatically by RevenueCat's androidx.startup initializer before onCreate. With no key
        //    the demo's FakeEntitlementProvider / FakeAnonymousIdentityProvider stay in place.
        val rcKey = BuildConfig.REVENUECAT_ANDROID_API_KEY
        val overrides =
            buildList<Module> {
                add(remoteConfigModule)
                add(databaseModule)
                add(demoNotesModule)
                if (rcKey.isNotBlank()) {
                    Purchases.configure(apiKey = rcKey)
                    add(revenueCatModule)
                    add(revenueCatIdentityModule)
                }
            }
        bootstrapDemoKoin(postHog = postHog, sentry = sentry) {
            allowOverride(true)
            // postHogAnalyticsModule reads the Application from Koin's androidContext (the Android
            // initializeFrnk overload registers it; the demo bypasses initializeFrnk, so do it here).
            androidContext(this@DemoApplication)
            modules(overrides)
        }
    }
}