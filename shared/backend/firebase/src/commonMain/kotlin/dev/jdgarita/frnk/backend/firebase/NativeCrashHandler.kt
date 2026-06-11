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
