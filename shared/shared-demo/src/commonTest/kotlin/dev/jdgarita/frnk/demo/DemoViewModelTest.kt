package dev.jdgarita.frnk.demo

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.monetization.DefaultEntitlementManager
import dev.jdgarita.frnk.monetization.FeatureGate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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
        // The real frnk EntitlementManager over the demo's fake provider — exactly the demo's wiring.
        val entitlements = DefaultEntitlementManager(FakeEntitlementProvider(), FakeKeyValueStore(), analytics, CoroutineScope(dispatcher))
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

    // --- Navigation as an effect + hoisted components state ---

    @Test
    fun request_upgrade_when_not_pro_emits_navigate_effect() =
        runTest(dispatcher) {
            // FakeEntitlementManager starts non-Pro, so RequestUpgrade falls through to a Navigate
            // effect (routed into the FrnkNavHost Paywall by routeDemoEffect, covered separately).
            val vm = viewModel(RecordingAnalytics(), RecordingCrash())
            runCurrent()
            val effects = mutableListOf<DemoEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.send(DemoIntent.RequestUpgrade)
            runCurrent()

            assertTrue(effects.any { it is DemoEffect.Navigate })
            job.cancel()
        }

    @Test
    fun god_mode_forces_pro_with_godmode_source() =
        runTest(dispatcher) {
            // God mode is a frnk-level override (independent of the provider): it flips isPro and
            // reports ProSource.GodMode.
            val vm = viewModel(RecordingAnalytics(), RecordingCrash())
            runCurrent()
            assertEquals(false, vm.state.value.isPro)

            vm.send(DemoIntent.ToggleGodMode)
            runCurrent()
            assertTrue(vm.state.value.isGodMode)
            assertTrue(vm.state.value.isPro)
            assertEquals("GodMode", vm.state.value.proSource)
        }

    @Test
    fun restore_with_no_purchase_emits_toast() =
        runTest(dispatcher) {
            // The fake provider has nothing to restore → a Toast, and the user stays Free.
            val vm = viewModel(RecordingAnalytics(), RecordingCrash())
            runCurrent()
            val effects = mutableListOf<DemoEffect>()
            val job = launch { vm.effects.toList(effects) }
            runCurrent()

            vm.send(DemoIntent.RestorePurchases)
            runCurrent()

            assertEquals(false, vm.state.value.isPro)
            assertTrue(effects.any { it is DemoEffect.Toast })
            job.cancel()
        }

    @Test
    fun search_open_query_then_close_clears_query() =
        runTest(dispatcher) {
            val vm = viewModel(RecordingAnalytics(), RecordingCrash())
            runCurrent()

            vm.send(DemoIntent.SearchOpened)
            vm.send(DemoIntent.SearchQueryChanged("Frnk"))
            runCurrent()
            assertTrue(vm.state.value.searchActive)
            assertEquals("Frnk", vm.state.value.searchQuery)

            vm.send(DemoIntent.SearchClosed)
            runCurrent()
            assertEquals(false, vm.state.value.searchActive)
            assertEquals("", vm.state.value.searchQuery)
        }

    @Test
    fun gallery_toggles_reduce_their_values() =
        runTest(dispatcher) {
            val vm = viewModel(RecordingAnalytics(), RecordingCrash())
            runCurrent()

            vm.send(DemoIntent.GallerySwitchChanged(false))
            vm.send(DemoIntent.GallerySegmentChanged(2))
            vm.send(DemoIntent.GalleryNavChanged(1))
            runCurrent()

            assertEquals(false, vm.state.value.gallerySwitchOn)
            assertEquals(2, vm.state.value.gallerySegmentIndex)
            assertEquals(1, vm.state.value.galleryNavIndex)
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
