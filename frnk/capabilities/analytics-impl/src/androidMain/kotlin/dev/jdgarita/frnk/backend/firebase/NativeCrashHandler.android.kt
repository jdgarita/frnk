package dev.jdgarita.frnk.backend.firebase

/**
 * No-op: the Crashlytics Android SDK already installs an uncaught-JVM-exception handler, so there is
 * no equivalent of the CrashKiOS hook to install here.
 */
internal actual fun enableNativeCrashHandler() = Unit