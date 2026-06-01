package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import org.koin.dsl.module

/**
 * Firebase analytics + crash reporting, decoupled from the auth/data backend (BACKLOG P1-5).
 *
 * `:shared`'s `frnkModules(observability = ObservabilityChoice.Firebase)` installs this. Because
 * it is independent of `BackendChoice`, a local-storage-only app (no backend) — or a Supabase-backed
 * app — can still ship Firebase Analytics + Crashlytics by selecting `ObservabilityChoice.Firebase`.
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
