package dev.jdgarita.frnk.demo.ui.home

import androidx.lifecycle.viewModelScope
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.camera.CameraController
import dev.jdgarita.frnk.demo.notes.NoteStore
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.FeatureGate
import dev.jdgarita.frnk.monetization.FrnkFeature
import dev.jdgarita.frnk.permissions.Permission
import dev.jdgarita.frnk.permissions.PermissionController
import dev.jdgarita.frnk.remoteconfig.RemoteConfigService
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.scaffolds.home.HomeScreenState
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.fold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DemoHomeViewModel(
    private val gate: FeatureGate,
    private val analytics: AnalyticsTracker,
    private val entitlements: EntitlementManager,
    private val notes: NoteStore,
    private val crash: CrashReporter,
    private val remoteConfig: RemoteConfigService,
    private val camera: CameraController,
    private val permissions: PermissionController
) : MviViewModel<DemoHomeArguments, DemoHomeModelState, DemoHomeScreenState, DemoHomeIntent, DemoHomeEffect>(
        factory = DemoHomeModelStateFactory
    ) {
    init {
        analytics.track(ToolkitEvent.AppOpened, mapOf("source" to "demo"))
        entitlements.status
            .onEach { status -> updateModel { copy(isPro = status.isPro, proSource = status.source.name) } }
            .launchIn(viewModelScope)
        entitlements.isGodMode
            .onEach { god -> updateModel { copy(isGodMode = god) } }
            .launchIn(viewModelScope)
        // Stage 11: seed the Remote Config value + current camera permission from their (no-op by
        // default) providers. androidDemoApp installs the real Firebase remoteConfigModule.
        updateModel {
            copy(
                remoteWelcome = remoteConfig.getString(REMOTE_WELCOME_KEY, "Hello from the no-op default"),
                cameraPermission = permissions.status(Permission.Camera).name
            )
        }
        viewModelScope.launch { loadNotes() }
    }

    private companion object {
        const val REMOTE_WELCOME_KEY = "demo_welcome_message"
    }

    private suspend fun loadNotes() {
        notes.all().fold(
            onSuccess = { stored -> updateModel { copy(notes = stored.map { it.content }) } },
            onFailure = { emit(DemoHomeEffect.Toast("Couldn't load notes: ${it.message}")) }
        )
    }

    override fun mapToUiState(modelState: DemoHomeModelState): DemoHomeScreenState =
        DemoHomeScreenState(
            email = "guy.turner@example.com",
            isPro = false,
            isGodMode = false,
            frnkHomeState =
                HomeScreenState(
                    topBar =
                        FrnkTopAppBarState(
                            title = FrnkStringSource.Raw("Frnk")
                        )
                )
        )

    override suspend fun onIntent(intent: DemoHomeIntent) {
        when (intent) {
            DemoHomeIntent.Increment -> updateModel { copy(count = count + 1) }
            DemoHomeIntent.Decrement -> updateModel { copy(count = count - 1) }
            is DemoHomeIntent.EmailChanged -> updateModel { copy(email = intent.value) }
            DemoHomeIntent.ToggleGodMode -> entitlements.setGodMode(!currentState().isGodMode)
            DemoHomeIntent.RefreshEntitlements -> {
                entitlements.refresh()
                emit(DemoHomeEffect.Toast("Refreshed entitlements (Pro=${currentState().isPro})"))
            }

            DemoHomeIntent.RestorePurchases ->
                when (val result = entitlements.restorePurchases()) {
                    is AppResult.Success ->
                        emit(DemoHomeEffect.Toast(if (result.data) "Purchases restored" else "Nothing to restore"))

                    is AppResult.Failure -> emit(DemoHomeEffect.Toast("Restore failed: ${result.error.message}"))
                }

            DemoHomeIntent.RequestUpgrade -> {
                if (gate.canUse(FrnkFeature.Premium)) {
                    emit(DemoHomeEffect.Toast("Already Pro — feature unlocked"))
                } else {
                    val route = gate.requestUpgrade(source = "demo_button")
                    emit(DemoHomeEffect.Navigate(route))
                }
            }

            DemoHomeIntent.AddNote ->
                notes
                    .add("Note #${currentState().notes.size + 1}")
                    .fold(
                        onSuccess = { loadNotes() },
                        onFailure = { emit(DemoHomeEffect.Toast("Couldn't save note: ${it.message}")) }
                    )

            DemoHomeIntent.ClearNotes ->
                notes.clear().fold(
                    onSuccess = { loadNotes() },
                    onFailure = { emit(DemoHomeEffect.Toast("Couldn't clear notes: ${it.message}")) }
                )

            DemoHomeIntent.TrackEvent -> {
                analytics.trackCustom("demo_button_tapped", mapOf("count" to currentState().count))
                emit(DemoHomeEffect.Toast("Tracked demo_button_tapped"))
            }

            DemoHomeIntent.SetUserProperty -> {
                analytics.setUserProperty("demo_tier", if (currentState().isPro) "pro" else "free")
                emit(DemoHomeEffect.Toast("Set user property demo_tier"))
            }

            DemoHomeIntent.LogBreadcrumb -> {
                crash.log("Demo breadcrumb @ count=${currentState().count}")
                emit(DemoHomeEffect.Toast("Logged crash breadcrumb"))
            }

            DemoHomeIntent.RecordTestCrash -> {
                crash.recordException(
                    RuntimeException("Demo non-fatal exception"),
                    mapOf("screen" to "demo", "count" to currentState().count.toString())
                )
                emit(DemoHomeEffect.Toast("Recorded non-fatal exception"))
            }

            DemoHomeIntent.ForceUnhandledCrash -> {
                // Throw an *uncaught* Kotlin exception on a background dispatcher with no handler, so it
                // escapes to the platform's uncaught-exception handler. On iOS that path is what the
                // CrashKiOS hook (installed by firebaseObservabilityModule) intercepts and forwards to
                // Crashlytics symbolicated — unlike RecordTestCrash above, which is an explicitly-caught
                // non-fatal. On Android the Crashlytics SDK's own handler catches it. The demo's logging
                // fakes have no such hook, so under DemoKit this simply terminates the process.
                emit(DemoHomeEffect.Toast("Forcing an unhandled crash…"))
                CoroutineScope(Dispatchers.Default).launch {
                    throw RuntimeException("Demo UNHANDLED Kotlin exception (exercises the iOS CrashKiOS hook)")
                }
            }

            DemoHomeIntent.SearchOpened -> updateModel { copy(searchActive = true) }
            DemoHomeIntent.SearchClosed -> updateModel { copy(searchActive = false, searchQuery = "") }
            is DemoHomeIntent.SearchQueryChanged -> updateModel { copy(searchQuery = intent.value) }
            is DemoHomeIntent.GallerySwitchChanged -> updateModel { copy(gallerySwitchOn = intent.checked) }
            is DemoHomeIntent.GallerySegmentChanged -> updateModel { copy(gallerySegmentIndex = intent.index) }
            is DemoHomeIntent.GalleryNavChanged -> updateModel { copy(galleryNavIndex = intent.index) }
            DemoHomeIntent.FetchRemoteConfig ->
                remoteConfig.fetchAndActivate().fold(
                    onSuccess = {
                        updateModel { copy(remoteWelcome = remoteConfig.getString(REMOTE_WELCOME_KEY, remoteWelcome)) }
                        emit(DemoHomeEffect.Toast("Remote Config fetched"))
                    },
                    onFailure = { emit(DemoHomeEffect.Toast("Fetch failed: ${it.message}")) }
                )

            DemoHomeIntent.CapturePhoto ->
                camera.capturePhoto().fold(
                    onSuccess = { image -> updateModel { copy(cameraResult = "Captured ${image.bytes.size} bytes") } },
                    onFailure = { updateModel { copy(cameraResult = "No camera (no-op scaffold)") } }
                )

            DemoHomeIntent.RequestCameraPermission -> {
                val status = permissions.request(Permission.Camera)
                updateModel { copy(cameraPermission = status.name) }
            }
        }
    }
}