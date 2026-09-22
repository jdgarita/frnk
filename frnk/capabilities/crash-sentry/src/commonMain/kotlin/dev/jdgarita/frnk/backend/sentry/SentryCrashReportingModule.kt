package dev.jdgarita.frnk.backend.sentry

import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.utils.PrintLogger
import io.sentry.kotlin.multiplatform.Sentry
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Sentry as the toolkit's one `CrashReporter`. Hosts don't install this themselves:
 * `frnkModules { observability(sentry = …) }` (`:ui-app`) does, from the config the host
 * passes. It stays public for the raw `initializeFrnk(modules = listOf(…))` path and for tests.
 *
 * The binding is `createdAtStart`, so `Sentry.init` runs inside `startKoin` on every bootstrap path,
 * with or without `validate`: a crash in the first frames after launch is still reported. The SDK is
 * started exactly once per process; a second Koin start (tests, a host that restarts its graph) sees
 * it already running and does not re-init. A missing DSN never reaches here —
 * [SentryCrashReportingConfig] rejects it at construction.
 */
fun sentryCrashReportingModule(config: SentryCrashReportingConfig): Module =
    module {
        single<CrashReporter>(createdAtStart = true) {
            startSentryOnce(config)
            SentryCrashReporter()
        }
    }

private var started = false

private fun startSentryOnce(config: SentryCrashReportingConfig) {
    if (started) return
    runCatching {
        Sentry.init { options ->
            options.dsn = config.dsn
            options.environment = config.environment
            options.release = config.release
            options.debug = config.debug
            options.sampleRate = config.sampleRate
            // Never IP / device names / headers: the toolkit's hosts are accountless and local-first.
            options.sendDefaultPii = false
            // Sentry's own recommendation for Compose Multiplatform on Apple: the C++ exception
            // monitor mis-attributes Kotlin/Native's exception machinery.
            options.enableUnhandledCppExceptionMonitoring = false
            config.configure(options)
        }
        started = true
    }.onFailure { PrintLogger.w(TAG, "Sentry.init failed, crash reporting is a no-op: ${it.message}") }
}

private const val TAG = "SentryCrash"