package dev.jdgarita.frnk.demo

import androidx.lifecycle.viewModelScope
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.database.NoteStore
import dev.jdgarita.frnk.monetization.Feature
import dev.jdgarita.frnk.monetization.FeatureGate
import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState
import dev.jdgarita.frnk.utils.fold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class DemoState(
    val count: Int = 0,
    val email: String = "",
    val isPro: Boolean = false,
    val notes: List<String> = emptyList(),
    // components tab — search state (which-screen-is-shown is owned by the nav back stack, not here)
    val searchActive: Boolean = false,
    val searchQuery: String = "",
    // components gallery widget demos
    val gallerySwitchOn: Boolean = true,
    val gallerySegmentIndex: Int = 0,
    val galleryNavIndex: Int = 0,
) : UiState

sealed interface DemoIntent : UiIntent {
    data object Increment : DemoIntent

    data object Decrement : DemoIntent

    data class EmailChanged(
        val value: String,
    ) : DemoIntent

    data object TogglePro : DemoIntent

    data object RequestUpgrade : DemoIntent

    data object AddNote : DemoIntent

    data object ClearNotes : DemoIntent

    data object TrackEvent : DemoIntent

    data object SetUserProperty : DemoIntent

    data object LogBreadcrumb : DemoIntent

    data object RecordTestCrash : DemoIntent

    data object ForceUnhandledCrash : DemoIntent

    // components tab — search
    data object SearchOpened : DemoIntent

    data object SearchClosed : DemoIntent

    data class SearchQueryChanged(
        val value: String,
    ) : DemoIntent

    // components gallery widget demos
    data class GallerySwitchChanged(
        val checked: Boolean,
    ) : DemoIntent

    data class GallerySegmentChanged(
        val index: Int,
    ) : DemoIntent

    data class GalleryNavChanged(
        val index: Int,
    ) : DemoIntent
}

sealed interface DemoEffect : UiEffect {
    data class Navigate(
        val routeKey: String,
    ) : DemoEffect

    data class Toast(
        val message: String,
    ) : DemoEffect
}

class DemoViewModel(
    private val gate: FeatureGate,
    private val analytics: AnalyticsTracker,
    private val entitlements: FakeEntitlementManager,
    private val notes: NoteStore,
    private val crash: CrashReporter,
) : MviViewModel<DemoState, DemoIntent, DemoEffect>(DemoState()) {
    init {
        analytics.track(ToolkitEvent.AppOpened, mapOf("source" to "demo"))
        gate.isPro
            .onEach { pro -> setState { copy(isPro = pro) } }
            .launchIn(viewModelScope)
        viewModelScope.launch { loadNotes() }
    }

    override suspend fun onIntent(intent: DemoIntent) {
        when (intent) {
            DemoIntent.Increment -> setState { copy(count = count + 1) }
            DemoIntent.Decrement -> setState { copy(count = count - 1) }
            is DemoIntent.EmailChanged -> setState { copy(email = intent.value) }
            DemoIntent.TogglePro -> entitlements.setPro(!currentState().isPro)
            DemoIntent.RequestUpgrade -> {
                if (gate.canUse(Feature.Premium)) {
                    emit(DemoEffect.Toast("Already Pro — feature unlocked"))
                } else {
                    val route = gate.requestUpgrade(source = "demo_button")
                    emit(DemoEffect.Navigate(route))
                }
            }
            DemoIntent.AddNote ->
                notes
                    .add("Note #${currentState().notes.size + 1}")
                    .fold(
                        onSuccess = { loadNotes() },
                        onFailure = { emit(DemoEffect.Toast("Couldn't save note: ${it.message}")) },
                    )
            DemoIntent.ClearNotes ->
                notes.clear().fold(
                    onSuccess = { loadNotes() },
                    onFailure = { emit(DemoEffect.Toast("Couldn't clear notes: ${it.message}")) },
                )
            DemoIntent.TrackEvent -> {
                analytics.trackCustom("demo_button_tapped", mapOf("count" to currentState().count))
                emit(DemoEffect.Toast("Tracked demo_button_tapped"))
            }
            DemoIntent.SetUserProperty -> {
                analytics.setUserProperty("demo_tier", if (currentState().isPro) "pro" else "free")
                emit(DemoEffect.Toast("Set user property demo_tier"))
            }
            DemoIntent.LogBreadcrumb -> {
                crash.log("Demo breadcrumb @ count=${currentState().count}")
                emit(DemoEffect.Toast("Logged crash breadcrumb"))
            }
            DemoIntent.RecordTestCrash -> {
                crash.recordException(
                    RuntimeException("Demo non-fatal exception"),
                    mapOf("screen" to "demo", "count" to currentState().count.toString()),
                )
                emit(DemoEffect.Toast("Recorded non-fatal exception"))
            }
            DemoIntent.ForceUnhandledCrash -> {
                // Throw an *uncaught* Kotlin exception on a background dispatcher with no handler, so it
                // escapes to the platform's uncaught-exception handler. On iOS that path is what the
                // CrashKiOS hook (installed by firebaseObservabilityModule) intercepts and forwards to
                // Crashlytics symbolicated — unlike RecordTestCrash above, which is an explicitly-caught
                // non-fatal. On Android the Crashlytics SDK's own handler catches it. The demo's logging
                // fakes have no such hook, so under DemoKit this simply terminates the process.
                emit(DemoEffect.Toast("Forcing an unhandled crash…"))
                CoroutineScope(Dispatchers.Default).launch {
                    throw RuntimeException("Demo UNHANDLED Kotlin exception (exercises the iOS CrashKiOS hook)")
                }
            }
            DemoIntent.SearchOpened -> setState { copy(searchActive = true) }
            DemoIntent.SearchClosed -> setState { copy(searchActive = false, searchQuery = "") }
            is DemoIntent.SearchQueryChanged -> setState { copy(searchQuery = intent.value) }
            is DemoIntent.GallerySwitchChanged -> setState { copy(gallerySwitchOn = intent.checked) }
            is DemoIntent.GallerySegmentChanged -> setState { copy(gallerySegmentIndex = intent.index) }
            is DemoIntent.GalleryNavChanged -> setState { copy(galleryNavIndex = intent.index) }
        }
    }

    private suspend fun loadNotes() {
        notes.all().fold(
            onSuccess = { stored -> setState { copy(notes = stored.map { it.content }) } },
            onFailure = { emit(DemoEffect.Toast("Couldn't load notes: ${it.message}")) },
        )
    }
}
