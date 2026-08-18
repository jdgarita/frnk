package dev.jdgarita.frnk.backend.firebase

/**
 * No-op: the Crashlytics Android SDK already installs an uncaught-JVM-exception handler, so there is
 * no equivalent of the CrashKiOS hook to install here.
 */
internal actual fun enableNativeCrashHandler() = Unit

/**
 * Always `false`: gitlive's Android `recordException` hands the throwable straight to the
 * Crashlytics Android SDK, which keeps the full JVM stack. There is nothing a native path
 * would add.
 */
internal actual fun recordNativeHandledException(throwable: Throwable): Boolean = false