package dev.jdgarita.frnk.backend

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Backend-independent no-op `AnalyticsTracker` binding — the `frnkModules { analytics = … }` default
 * for hosts that don't want product analytics. Assign a provider module (e.g. PostHog's) to the slot
 * instead of this one, never both: a second binding silently shadows the first.
 */
val noopAnalyticsModule: Module =
    module {
        single<AnalyticsTracker> { NoopAnalyticsTracker() }
    }

/**
 * Backend-independent no-op `CrashReporter` binding — the `frnkModules { crashReporting = … }`
 * default for hosts that don't want crash reporting. Same XOR rule as [noopAnalyticsModule].
 */
val noopCrashReportingModule: Module =
    module {
        single<CrashReporter> { NoopCrashReporter() }
    }

/**
 * Both no-op bindings in one module, for the raw `initializeFrnk(modules = listOf(…))` path and for
 * tests that want a telemetry-free graph in one line. `frnkModules { }` reaches for the two halves
 * separately so each slot stays independently replaceable.
 */
val noopObservabilityModule: Module =
    module {
        includes(noopAnalyticsModule, noopCrashReportingModule)
    }