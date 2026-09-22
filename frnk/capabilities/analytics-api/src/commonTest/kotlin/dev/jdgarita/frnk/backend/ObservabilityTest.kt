package dev.jdgarita.frnk.backend

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Covers the recording fakes (BACKLOG P1-5): they capture what was emitted, so downstream tests can
 * assert on them. There is no production no-op to cover — every host installs PostHog + Sentry.
 */
class ObservabilityTest {
    @Test
    fun fake_analytics_records_events_and_properties() {
        val analytics = FakeAnalyticsTracker()

        analytics.track(ToolkitEvent.AppOpened, mapOf("source" to "test"))
        analytics.trackCustom("custom_event", mapOf("count" to 3))
        analytics.screen("home", mapOf("tab" to "vault"))
        analytics.setUserProperty("tier", "pro")

        assertEquals(
            listOf("app_opened", "custom_event"),
            analytics.tracked.map { it.name }
        )
        assertEquals(3, analytics.tracked[1].params["count"])
        assertEquals(listOf("home"), analytics.screens.map { it.name }, "screens are recorded apart from events")
        assertEquals("vault", analytics.screens.single().params["tab"])
        assertEquals("pro", analytics.userProperties["tier"])
    }

    @Test
    fun fake_crash_records_exceptions_logs_and_user_id() =
        runTest {
            val crash = FakeCrashReporter()
            val error = IllegalStateException("non-fatal")

            crash.log("entered screen")
            crash.identify("uid-42")
            crash.recordException(error, mapOf("screen" to "demo"))

            assertEquals(listOf("entered screen"), crash.logs)
            assertEquals("uid-42", crash.userId)
            assertEquals(1, crash.recorded.size)
            assertEquals(error, crash.recorded.single().throwable)
            assertTrue(crash.recorded.single().extras["screen"] == "demo")
        }
}