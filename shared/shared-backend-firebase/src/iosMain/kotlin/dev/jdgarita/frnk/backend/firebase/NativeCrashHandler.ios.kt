package dev.jdgarita.frnk.backend.firebase

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook
import dev.jdgarita.frnk.utils.PrintLogger

private var installed = false

/**
 * Installs the CrashKiOS unhandled-exception hook and points it at the native Crashlytics SDK.
 * Wrapped in [runCatching] (mirroring [FirebaseCrashReporter]) so a missing/misconfigured native
 * Crashlytics framework degrades to a logged no-op instead of crashing. The [installed] guard keeps
 * it idempotent across repeated `startKoin`/`stopKoin` cycles (tests).
 */
internal actual fun enableNativeCrashHandler() {
    if (installed) return
    runCatching {
        enableCrashlytics()
        setCrashlyticsUnhandledExceptionHook()
        installed = true
    }.onFailure { PrintLogger.w(TAG, "native crash handler install skipped: ${it.message}") }
}

private const val TAG = "FirebaseCrash"
