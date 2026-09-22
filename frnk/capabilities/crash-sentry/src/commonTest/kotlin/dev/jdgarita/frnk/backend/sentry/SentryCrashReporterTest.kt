package dev.jdgarita.frnk.backend.sentry

import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.test.runTest
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SentryCrashReporterTest {
    @Test
    fun `non fatal carries its extras and breadcrumbs and user id reach the sdk`() =
        runTest {
            val gateway = RecordingSentryGateway()
            val reporter = SentryCrashReporter(gateway)
            val error = IllegalStateException("boom")

            reporter.log("opened scanner")
            reporter.identify("uid-1")
            reporter.recordException(error, mapOf("screen" to "scanner"))

            assertEquals(listOf("opened scanner"), gateway.breadcrumbs)
            assertEquals("uid-1", gateway.userId)
            assertEquals(error to mapOf("screen" to "scanner"), gateway.captured.single())
        }

    @Test
    fun `sdk failures degrade to a logged no-op`() =
        runTest {
            val reporter = SentryCrashReporter(ThrowingSentryGateway())

            reporter.log("still fine")
            reporter.recordException(RuntimeException("x"))

            assertEquals(AppResult.Failure(IdentityError.Error), reporter.identify("uid"))
        }

    @Test
    fun `a blank dsn is a configuration error, not a silent no-op`() {
        // Every host ships real crash reporting, so a missing DSN must fail where it is written — the
        // config — and name the fix, instead of binding a reporter that drops every crash.
        val failure = assertFailsWith<IllegalArgumentException> { SentryCrashReportingConfig(dsn = "", environment = "test") }
        assertTrue(failure.message.orEmpty().contains("dsn"), "names the field")
    }

    @Test
    fun `a configured module starts the sdk at koin start and binds the sentry reporter`() {
        // Sentry.init on the JVM host has no Android context to attach; the module's runCatching
        // keeps startKoin alive either way, which is the contract this pins.
        val config = SentryCrashReportingConfig(dsn = "https://key@o1.ingest.sentry.io/1", environment = "test")
        val app = koinApplication { modules(sentryCrashReportingModule(config)) }
        try {
            assertIs<SentryCrashReporter>(app.koin.get<CrashReporter>())
        } finally {
            app.close()
        }
    }
}

private class RecordingSentryGateway : SentryGateway {
    val captured = mutableListOf<Pair<Throwable, Map<String, String>>>()
    val breadcrumbs = mutableListOf<String>()
    var userId: String? = null

    override fun captureException(
        throwable: Throwable,
        extras: Map<String, String>
    ) {
        captured += throwable to extras
    }

    override fun addBreadcrumb(message: String) {
        breadcrumbs += message
    }

    override fun setUser(id: String) {
        userId = id
    }
}

private class ThrowingSentryGateway : SentryGateway {
    override fun captureException(
        throwable: Throwable,
        extras: Map<String, String>
    ): Unit = error("sdk down")

    override fun addBreadcrumb(message: String): Unit = error("sdk down")

    override fun setUser(id: String): Unit = error("sdk down")
}