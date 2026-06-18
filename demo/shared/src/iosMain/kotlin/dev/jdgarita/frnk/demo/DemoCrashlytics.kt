package dev.jdgarita.frnk.demo

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook
import dev.jdgarita.frnk.utils.PrintLogger

private var installed = false

/**
 * Installs the CrashKiOS unhandled-exception hook for the iOS demo so the "Force crash" panic button
 * (an uncaught Kotlin exception) is reported to Firebase Crashlytics — the same two CrashKiOS calls
 * the production `:shared:backend:firebase` path makes. Call once from Swift **after**
 * `FirebaseApp.configure()`.
 *
 * The host (`iosDemoApp`) must link the native Firebase Crashlytics SDK (added via SPM/CocoaPods) and
 * ship `GoogleService-Info.plist`. This is demo-only: it wires CrashKiOS directly here (rather than
 * importing the `:shared:backend:firebase` impl) so `DemoKit` gains only the lightweight CrashKiOS
 * cinterop, not the full gitlive Firebase stack. [installed] keeps it idempotent.
 */
fun enableDemoCrashlytics() {
    if (installed) return
    runCatching {
        enableCrashlytics()
        setCrashlyticsUnhandledExceptionHook()
        installed = true
    }.onFailure { PrintLogger.w("DemoCrash", "crash handler install skipped: ${it.message}") }
}