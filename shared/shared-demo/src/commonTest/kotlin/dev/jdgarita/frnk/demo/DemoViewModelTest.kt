package dev.jdgarita.frnk.demo

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.monetization.FeatureGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Reducer-level coverage for the analytics/crash wiring added in BACKLOG P1-5: the new intents must
 * route through the injected [AnalyticsTracker] / [CrashReporter], and `AppOpened` must fire on init.
 * Uses local recording fakes (the `shared-backend-api` fakes live in that module's `commonTest`, which
 * isn't visible here). Follows the `MviViewModelTest` template: `Dispatchers.setMain` drives the
 * `viewModelScope` intent collector.
 */
class DemoViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        analytics: AnalyticsTracker,
        crash: CrashReporter,
    ): DemoViewModel {
        val entitlements = FakeEntitlementManager()
        val gate = FeatureGate(entitlements, analytics)
        return DemoViewModel(gate, analytics, entitlements, FakeNoteStore(), crash)
    }

    @Test
    fun init_tracks_app_opened() =
        runTest(dispatcher) {
            val analytics = RecordingAnalytics()
            viewModel(analytics, RecordingCrash())
            runCurrent()
            assertTrue(analytics.tracked.contains(ToolkitEvent.AppOpened.key))
        }

    @Test
    fun track_event_intent_records_custom_event() =
        runTest(dispatcher) {
            val analytics = RecordingAnalytics()
            val vm = viewModel(analytics, RecordingCrash())
            runCurrent()

            vm.send(DemoIntent.TrackEvent)
            runCurrent()

            assertTrue(analytics.tracked.contains("demo_button_tapped"))
        }

    @Test
    fun set_user_property_intent_sets_tier() =
        runTest(dispatcher) {
            val analytics = RecordingAnalytics()
            val vm = viewModel(analytics, RecordingCrash())
            runCurrent()

            vm.send(DemoIntent.SetUserProperty)
            runCurrent()

            assertEquals("free", analytics.userProperties["demo_tier"])
        }

    @Test
    fun crash_intents_log_and_record() =
        runTest(dispatcher) {
            val crash = RecordingCrash()
            val vm = viewModel(RecordingAnalytics(), crash)
            runCurrent()

            vm.send(DemoIntent.LogBreadcrumb)
            vm.send(DemoIntent.RecordTestCrash)
            runCurrent()

            assertEquals(1, crash.logs.size)
            assertEquals(1, crash.exceptions.size)
        }
}

private class RecordingAnalytics : AnalyticsTracker {
    val tracked = mutableListOf<String>()
    val userProperties = mutableMapOf<String, String?>()

    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>,
    ) {
        tracked += event.key
    }

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>,
    ) {
        tracked += name
    }

    override fun setUserProperty(
        key: String,
        value: String?,
    ) {
        userProperties[key] = value
    }
}

private class RecordingCrash : CrashReporter {
    val exceptions = mutableListOf<Throwable>()
    val logs = mutableListOf<String>()

    override fun recordException(
        throwable: Throwable,
        extras: Map<String, String>,
    ) {
        exceptions += throwable
    }

    override fun setUserId(id: String?) = Unit

    override fun log(message: String) {
        logs += message
    }
}
