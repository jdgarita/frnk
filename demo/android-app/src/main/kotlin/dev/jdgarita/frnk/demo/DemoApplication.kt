package dev.jdgarita.frnk.demo

import android.app.Application
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import dev.jdgarita.frnk.backend.firebase.firebaseObservabilityModule
import dev.jdgarita.frnk.database.impl.databaseModule
import dev.jdgarita.frnk.demo.notes.demoNotesModule
import dev.jdgarita.frnk.di.DatabaseContext
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatModule
import dev.jdgarita.frnk.remoteconfig.firebase.remoteConfigModule
import dev.jdgarita.frnk.ui.app.frnkModules

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // The real SQL driver path resolves the Application context through this seam; hosts on
        // initializeFrnk(context, …) get it set automatically, but the demo boots bootstrapDemoKoin().
        DatabaseContext.application = this
        // Demo wiring uses logging/in-memory fakes everywhere (so DemoKit/iOS stay SDK-free), but on
        // Android we override selected bindings with the REAL toolkit modules to smoke-test the SDKs
        // on a device:
        //  - firebaseObservabilityModule — real Firebase Analytics + Crashlytics (BACKLOG P1-5).
        //  - databaseModule + demoNotesModule — the real SQLDelight path: the toolkit's
        //    SqlDriverFactory (:data-db-impl) building the demo-owned DemoDB schema, replacing
        //    the in-memory FakeNoteStore (restructure Stage 4 / OQ-2).
        //  - remoteConfigModule — real Firebase Remote Config (restructure Stage 11), replacing the
        //    no-op default so the demo's "Capabilities" section shows a live fetched value when a
        //    `demo_welcome_message` parameter is set in the frnk-demo Firebase project (else the
        //    bundled default). google-services.json + the google-services plugin already init Firebase.
        //  - revenueCatModule — real RevenueCat EntitlementManager (BACKLOG P3-2), installed only when
        //    a public Android SDK key is present in local.properties. Purchases.configure(...) must run
        //    before the override so the manager reads a configured SDK; the Android context is captured
        //    automatically by RevenueCat's androidx.startup initializer before onCreate.
        // Assemble the real-SDK override list with the toolkit's own frnkModules { } builder (Tier 2.2):
        // single observability/remoteConfig slots make the XOR explicit, and monetization(provider)
        // bundles the trio. The host still imports the impl vals and assigns them here.
        val rcKey = BuildConfig.REVENUECAT_ANDROID_API_KEY
        val rcConfigured = rcKey.isNotBlank()
        if (rcConfigured) {
            Purchases.configure(apiKey = rcKey)
        }
        val overrides =
            frnkModules {
                observability = firebaseObservabilityModule
                remoteConfig = remoteConfigModule
                if (rcConfigured) monetization(provider = revenueCatModule)
                modules(databaseModule, demoNotesModule)
            }
        bootstrapDemoKoin {
            allowOverride(true)
            modules(overrides)
        }
    }
}