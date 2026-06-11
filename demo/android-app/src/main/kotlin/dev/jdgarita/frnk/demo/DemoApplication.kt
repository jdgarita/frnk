package dev.jdgarita.frnk.demo

import android.app.Application
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import dev.jdgarita.frnk.backend.firebase.firebaseObservabilityModule
import dev.jdgarita.frnk.monetization.revenuecat.revenueCatModule
import org.koin.core.module.Module

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Demo wiring uses logging/in-memory fakes everywhere (so DemoKit/iOS stay SDK-free), but on
        // Android we override selected bindings with the REAL toolkit modules to smoke-test the SDKs
        // on a device:
        //  - firebaseObservabilityModule — real Firebase Analytics + Crashlytics (BACKLOG P1-5).
        //  - revenueCatModule — real RevenueCat EntitlementManager (BACKLOG P3-2), installed only when
        //    a public Android SDK key is present in local.properties. Purchases.configure(...) must run
        //    before the override so the manager reads a configured SDK; the Android context is captured
        //    automatically by RevenueCat's androidx.startup initializer before onCreate.
        val overrides = mutableListOf<Module>(firebaseObservabilityModule)
        val rcKey = BuildConfig.REVENUECAT_ANDROID_API_KEY
        if (rcKey.isNotBlank()) {
            Purchases.configure(apiKey = rcKey)
            overrides += revenueCatModule
        }
        bootstrapDemoKoin {
            allowOverride(true)
            modules(overrides)
        }
    }
}
