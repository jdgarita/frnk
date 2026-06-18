package dev.jdgarita.frnk.backend

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Backend-independent no-op analytics/crash bindings — the default for hosts that don't want
 * telemetry. Install this **or** `firebaseObservabilityModule` (in `:shared:backend:firebase`) in
 * the explicit module list passed to `initializeFrnk(...)`, never both.
 */
val noopObservabilityModule: Module =
    module {
        single<AnalyticsTracker> { NoopAnalyticsTracker() }
        single<CrashReporter> { NoopCrashReporter() }
    }