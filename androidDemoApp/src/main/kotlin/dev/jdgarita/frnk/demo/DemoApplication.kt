package dev.jdgarita.frnk.demo

import android.app.Application
import dev.jdgarita.frnk.backend.firebase.firebaseObservabilityModule

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Demo wiring uses logging fakes everywhere (so DemoKit/iOS stay SDK-free), but on Android
        // we override the analytics/crash bindings with the REAL firebaseObservabilityModule to
        // smoke-test Firebase Analytics + Crashlytics on a device (BACKLOG P1-5). google-services.json
        // is processed by the google-services Gradle plugin so Firebase auto-inits at startup.
        bootstrapDemoKoin {
            allowOverride(true)
            modules(firebaseObservabilityModule)
        }
    }
}
