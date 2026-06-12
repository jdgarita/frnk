package dev.jdgarita.frnk.demo

import androidx.lifecycle.viewModelScope
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.camera.CameraController
import dev.jdgarita.frnk.demo.notes.NoteStore
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.Feature
import dev.jdgarita.frnk.monetization.FeatureGate
import dev.jdgarita.frnk.monetization.ProSource
import dev.jdgarita.frnk.permissions.Permission
import dev.jdgarita.frnk.permissions.PermissionController
import dev.jdgarita.frnk.remoteconfig.RemoteConfigService
import dev.jdgarita.frnk.ui.bottomnav.FrnkAdaptiveNavEngine
import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState
import dev.jdgarita.frnk.utils.AppResult
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
    val isGodMode: Boolean = false,
    // Where Pro comes from (None / Purchase / GodMode) — shown in the FeatureGate demo section.
    val proSource: String = ProSource.None.name,
    val notes: List<String> = emptyList(),
    // components tab — search state (which-screen-is-shown is owned by the nav back stack, not here)
    val searchActive: Boolean = false,
    val searchQuery: String = "",
    // components gallery widget demos
    val gallerySwitchOn: Boolean = true,
    val gallerySegmentIndex: Int = 0,
    val galleryNavIndex: Int = 0,
    // POC: which adaptive bottom-bar engine FrnkTabbedNavScaffold renders (A/B Calf vs adaptive-nav-bar).
    val navEngine: FrnkAdaptiveNavEngine = FrnkAdaptiveNavEngine.Calf,
    // Stage 11 capability scaffolds — Remote Config value + camera/permission no-op outcomes.
    val remoteWelcome: String = "",
    val cameraResult: String = "Not captured",
    val cameraPermission: String = "",
) : UiState

sealed interface DemoIntent : UiIntent {
    data object Increment : DemoIntent

    data object Decrement : DemoIntent

    data class EmailChanged(
        val value: String,
    ) : DemoIntent

    data object ToggleGodMode : DemoIntent

    data object RefreshEntitlements : DemoIntent

    data object RestorePurchases : DemoIntent

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

    // POC: switch the adaptive bottom-bar engine (0 = Calf, 1 = AdaptiveNavBar).
    data class NavEngineChanged(
        val index: Int,
    ) : DemoIntent

    // Stage 11 capability scaffolds.
    data object FetchRemoteConfig : DemoIntent

    data object CapturePhoto : DemoIntent

    data object RequestCameraPermission : DemoIntent
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
    private val entitlements: EntitlementManager,
    private val notes: NoteStore,
    private val crash: CrashReporter,
    private val remoteConfig: RemoteConfigService,
    private val camera: CameraController,
    private val permissions: PermissionController,
) : MviViewModel<DemoState, DemoIntent, DemoEffect>(DemoState()) {
    init {
        analytics.track(ToolkitEvent.AppOpened, mapOf("source" to "demo"))
        entitlements.status
            .onEach { status -> setState { copy(isPro = status.isPro, proSource = status.source.name) } }
            .launchIn(viewModelScope)
        entitlements.isGodMode
            .onEach { god -> setState { copy(isGodMode = god) } }
            .launchIn(viewModelScope)
        // Stage 11: seed the Remote Config value + current camera permission from their (no-op by
        // default) providers. androidDemoApp installs the real Firebase remoteConfigModule.
        setState {
            copy(
                remoteWelcome = remoteConfig.getString(REMOTE_WELCOME_KEY, "Hello from the no-op default"),
                cameraPermission = permissions.status(Permission.Camera).name,
            )
        }
        viewModelScope.launch { loadNotes() }
    }

    override suspend fun onIntent(intent: DemoIntent) {
        when (intent) {
            DemoIntent.Increment -> setState { copy(count = count + 1) }
            DemoIntent.Decrement -> setState { copy(count = count - 1) }
            is DemoIntent.EmailChanged -> setState { copy(email = intent.value) }
            DemoIntent.ToggleGodMode -> entitlements.setGodMode(!currentState().isGodMode)
            DemoIntent.RefreshEntitlements -> {
                entitlements.refresh()
                emit(DemoEffect.Toast("Refreshed entitlements (Pro=${currentState().isPro})"))
            }
            DemoIntent.RestorePurchases ->
                when (val result = entitlements.restorePurchases()) {
                    is AppResult.Success ->
                        emit(DemoEffect.Toast(if (result.data) "Purchases restored" else "Nothing to restore"))
                    is AppResult.Failure -> emit(DemoEffect.Toast("Restore failed: ${result.error.message}"))
                }
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
            is DemoIntent.NavEngineChanged ->
                setState {
                    copy(
                        navEngine =
                            if (intent.index == 0) FrnkAdaptiveNavEngine.Calf else FrnkAdaptiveNavEngine.AdaptiveNavBar,
                    )
                }
            DemoIntent.FetchRemoteConfig ->
                remoteConfig.fetchAndActivate().fold(
                    onSuccess = {
                        setState { copy(remoteWelcome = remoteConfig.getString(REMOTE_WELCOME_KEY, remoteWelcome)) }
                        emit(DemoEffect.Toast("Remote Config fetched"))
                    },
                    onFailure = { emit(DemoEffect.Toast("Fetch failed: ${it.message}")) },
                )
            DemoIntent.CapturePhoto ->
                camera.capturePhoto().fold(
                    onSuccess = { image -> setState { copy(cameraResult = "Captured ${image.bytes.size} bytes") } },
                    onFailure = { setState { copy(cameraResult = "No camera (no-op scaffold)") } },
                )
            DemoIntent.RequestCameraPermission -> {
                val status = permissions.request(Permission.Camera)
                setState { copy(cameraPermission = status.name) }
            }
        }
    }

    private companion object {
        const val REMOTE_WELCOME_KEY = "demo_welcome_message"
    }

    private suspend fun loadNotes() {
        notes.all().fold(
            onSuccess = { stored -> setState { copy(notes = stored.map { it.content }) } },
            onFailure = { emit(DemoEffect.Toast("Couldn't load notes: ${it.message}")) },
        )
    }
}
