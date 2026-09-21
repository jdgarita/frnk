package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import org.koin.core.module.Module
import org.koin.dsl.module

/** Firebase Analytics as the `AnalyticsTracker` — assign to `frnkModules { analytics = … }`. */
val firebaseAnalyticsModule: Module =
    module {
        single<AnalyticsTracker> { FirebaseAnalyticsTracker() }
    }

/**
 * Firebase Crashlytics as the `CrashReporter` — assign to `frnkModules { crashReporting = … }`.
 * Resolving the binding installs the iOS unhandled-Kotlin-exception hook (no-op on Android).
 */
val firebaseCrashReportingModule: Module =
    module {
        single<CrashReporter> {
            // Install the iOS unhandled-Kotlin-exception hook (no-op on Android) exactly when
            // Firebase crash reporting is selected and the reporter is first resolved.
            enableNativeCrashHandler()
            FirebaseCrashReporter()
        }
    }

/**
 * Firebase analytics + crash reporting in one module, decoupled from the auth/data backend
 * (BACKLOG P1-5), for the raw `initializeFrnk(modules = listOf(…))` path. `frnkModules { }` has one
 * slot per half, so assign [firebaseAnalyticsModule] and [firebaseCrashReportingModule] there.
 */
val firebaseObservabilityModule: Module =
    module {
        includes(firebaseAnalyticsModule, firebaseCrashReportingModule)
    }