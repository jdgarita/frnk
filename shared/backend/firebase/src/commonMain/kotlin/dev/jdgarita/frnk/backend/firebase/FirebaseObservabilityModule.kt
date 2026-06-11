package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import org.koin.dsl.module

/**
 * Firebase analytics + crash reporting, decoupled from the auth/data backend (BACKLOG P1-5).
 *
 * Hosts install this (or `noopObservabilityModule` from `:shared:backend:api`) in the explicit
 * module list passed to `initializeFrnk(...)`. Because it is independent of the data backend, a
 * local-storage-only app can still ship Firebase Analytics + Crashlytics.
 */
val firebaseObservabilityModule =
    module {
        single<AnalyticsTracker> { FirebaseAnalyticsTracker() }
        single<CrashReporter> {
            // Install the iOS unhandled-Kotlin-exception hook (no-op on Android) exactly when
            // Firebase observability is selected and the reporter is first resolved.
            enableNativeCrashHandler()
            FirebaseCrashReporter()
        }
    }
