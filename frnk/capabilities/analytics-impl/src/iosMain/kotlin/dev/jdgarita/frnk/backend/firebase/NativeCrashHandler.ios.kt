package dev.jdgarita.frnk.backend.firebase

import co.touchlab.crashkios.crashlytics.CrashlyticsKotlin
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

/**
 * Reports through CrashKiOS, which turns the Kotlin stack *addresses* into `FIRStackFrame`s on a
 * real `FIRExceptionModel` — the consumer's dSYM then symbolicates them into Kotlin frames. This is
 * the whole reason iOS does not use gitlive's `recordException` here; see the expect declaration.
 *
 * Gated on [installed] because `CrashlyticsKotlin` starts out backed by CrashKiOS's no-op
 * `EmptyCalls`: without [enableNativeCrashHandler] having run, the call would silently succeed and
 * report nothing, and returning `true` would suppress the gitlive fallback.
 */
internal actual fun recordNativeHandledException(throwable: Throwable): Boolean {
    if (!installed) return false
    return runCatching { CrashlyticsKotlin.sendHandledException(throwable) }
        .onFailure { PrintLogger.w(TAG, "handled exception skipped: ${it.message}") }
        .isSuccess
}

private const val TAG = "FirebaseCrash"