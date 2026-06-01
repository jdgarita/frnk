package dev.jdgarita.frnk.shared

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.NoopAnalyticsTracker
import dev.jdgarita.frnk.backend.NoopCrashReporter
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Analytics + crash reporting provider, selected **independently of [BackendChoice]** (BACKLOG P1-5).
 *
 * - [None] — no-op defaults ([noopObservabilityModule]). Safe for apps that don't want telemetry.
 * - [Firebase] — Firebase Analytics + Crashlytics (`firebaseObservabilityModule`), regardless of
 *   which (if any) auth/data backend is chosen.
 */
enum class ObservabilityChoice { None, Firebase }

/** Backend-independent no-op analytics/crash bindings — the default for [ObservabilityChoice.None]. */
val noopObservabilityModule: Module =
    module {
        single<AnalyticsTracker> { NoopAnalyticsTracker() }
        single<CrashReporter> { NoopCrashReporter() }
    }
