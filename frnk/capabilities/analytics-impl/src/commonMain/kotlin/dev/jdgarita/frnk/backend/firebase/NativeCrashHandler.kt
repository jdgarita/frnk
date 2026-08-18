package dev.jdgarita.frnk.backend.firebase

/**
 * Installs the platform's native unhandled-crash hook for Firebase Crashlytics.
 *
 * iOS installs the CrashKiOS Kotlin/Native unhandled-exception hook so that *uncaught* Kotlin
 * exceptions reach Crashlytics symbolicated — gitlive's [FirebaseCrashReporter.recordException]
 * only reports exceptions the app explicitly catches, and an uncaught Kotlin exception otherwise
 * aborts via `konan` with no usable stack frame. Android is a no-op: the Crashlytics Android SDK
 * already hooks uncaught JVM exceptions.
 *
 * Invoked once, lazily, when `firebaseObservabilityModule` is installed and the `CrashReporter`
 * binding is first resolved (see [firebaseObservabilityModule]). Safe to call before the consumer
 * calls `FirebaseApp.configure()`: the hook only needs Crashlytics live at crash time.
 */
internal expect fun enableNativeCrashHandler()

/**
 * Records [throwable] as a Crashlytics **non-fatal** through the platform's native path, when that
 * path preserves Kotlin stack frames. Returns `true` when it reported, so the caller can fall back
 * to gitlive's `recordException` on `false`.
 *
 * iOS needs this because gitlive's actual is `FIRCrashlytics.recordError(throwable.asNSError())`:
 * the Kotlin throwable goes into `userInfo["KotlinException"]` where Crashlytics cannot read it,
 * and the report gets whatever Objective-C stack happened to be on the call site rather than the
 * throw site. CrashKiOS builds a real `FIRExceptionModel` out of the Kotlin stack addresses
 * instead, which the dSYM symbolicates server-side. Android returns `false`: the Crashlytics
 * Android SDK already keeps the full JVM stack, so gitlive is the better path there.
 *
 * See [enableNativeCrashHandler] — the iOS actual reports only once that hook is installed.
 */
internal expect fun recordNativeHandledException(throwable: Throwable): Boolean