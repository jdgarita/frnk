package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.CrashReporter
import org.koin.dsl.koinApplication
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Host-test coverage for the CrashKiOS wiring (BACKLOG P1-5b). Runs on the JVM via `testAndroidHostTest`,
 * so it exercises the **Android** `enableNativeCrashHandler` actual (the no-op) — the iOS actual that
 * actually calls CrashKiOS is only compiled/linked on macOS and verified there (CI skips iOS targets).
 *
 * What this guards on the CI path: the install hook is a safe no-op on the JVM, and resolving
 * `CrashReporter` from [firebaseObservabilityModule] (which invokes the hook on first resolve) does not
 * throw even with no Firebase configured.
 */
class FirebaseObservabilityModuleTest {
    @Test
    fun enableNativeCrashHandler_isSafeNoOpOnJvm() {
        // androidMain actual: must be a callable no-op (no exception).
        enableNativeCrashHandler()
        enableNativeCrashHandler() // idempotent — second call is also safe.
    }

    @Test
    fun crashReporter_resolvesWithoutThrowing() {
        val app = koinApplication { modules(firebaseObservabilityModule) }
        try {
            // Resolving triggers enableNativeCrashHandler() (no-op on JVM) then constructs the reporter.
            val reporter = app.koin.get<CrashReporter>()
            assertNotNull(reporter)
        } finally {
            app.close()
        }
    }

    @AfterTest
    fun tearDown() = Unit
}
