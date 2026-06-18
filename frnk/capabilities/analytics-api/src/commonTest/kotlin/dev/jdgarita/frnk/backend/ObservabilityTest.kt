package dev.jdgarita.frnk.backend

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Covers the observability defaults + recording fakes (BACKLOG P1-5):
 *  - the [NoopAnalyticsTracker] / [NoopCrashReporter] defaults are inert (never throw, record nothing),
 *  - the recording fakes capture what was emitted, so downstream tests can assert on them.
 */
class ObservabilityTest {
    @Test
    fun noop_observability_is_inert() {
        // No state to observe — the contract is simply "never throws".
        NoopAnalyticsTracker().apply {
            track(ToolkitEvent.AppOpened, mapOf("source" to "test"))
            trackCustom("custom", mapOf("n" to 1))
            setUserProperty("tier", "pro")
        }
        NoopCrashReporter().apply {
            recordException(RuntimeException("boom"), mapOf("k" to "v"))
            setUserId("uid")
            log("breadcrumb")
        }
    }

    @Test
    fun fake_analytics_records_events_and_properties() {
        val analytics = FakeAnalyticsTracker()

        analytics.track(ToolkitEvent.AppOpened, mapOf("source" to "test"))
        analytics.trackCustom("custom_event", mapOf("count" to 3))
        analytics.setUserProperty("tier", "pro")

        assertEquals(
            listOf("App_Opened", "custom_event"),
            analytics.tracked.map { it.name }
        )
        assertEquals(3, analytics.tracked[1].params["count"])
        assertEquals("pro", analytics.userProperties["tier"])
    }

    @Test
    fun fake_crash_records_exceptions_logs_and_user_id() {
        val crash = FakeCrashReporter()
        val error = IllegalStateException("non-fatal")

        crash.log("entered screen")
        crash.setUserId("uid-42")
        crash.recordException(error, mapOf("screen" to "demo"))

        assertEquals(listOf("entered screen"), crash.logs)
        assertEquals("uid-42", crash.userId)
        assertEquals(1, crash.recorded.size)
        assertEquals(error, crash.recorded.single().throwable)
        assertTrue(crash.recorded.single().extras["screen"] == "demo")
    }
}