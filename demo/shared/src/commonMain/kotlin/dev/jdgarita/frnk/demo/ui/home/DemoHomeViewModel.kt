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
import dev.jdgarita.frnk.notifications.LocalNotificationScheduler
import dev.jdgarita.frnk.notifications.NotificationPermissionStatus
import dev.jdgarita.frnk.permissions.Permission
import dev.jdgarita.frnk.permissions.PermissionController
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class DemoHomeViewModel(
    private val gate: FeatureGate,
    private val analytics: AnalyticsTracker,
    private val entitlements: EntitlementManager,
    private val notes: NoteStore,
    private val crash: CrashReporter,
    private val camera: CameraController,
    private val permissions: PermissionController,
    private val notifications: LocalNotificationScheduler
) : MviViewModel<DemoHomeArguments, DemoHomeModelState, DemoHomeScreenState, DemoHomeIntent, DemoHomeEffect>(
        factory = DemoHomeModelStateFactory,
        mapper = ::demoHomeScreenState
    ) {
    init {
        analytics.track(ToolkitEvent.AppOpened, mapOf("source" to "demo"))
        entitlements.status
            .onEach { status ->
                updateModel {
                    copy(
                        isPro = status.isPro,
                        proSource = status.source.name,
                        isTrial = status.isTrial,
                        expiresAt = status.expiresAt?.toString() ?: "—",
                        willRenew = status.willRenew
                    )
                }
            }.launchIn(viewModelScope)
        entitlements.isGodMode
            .onEach { god -> updateModel { copy(isGodMode = god) } }
            .launchIn(viewModelScope)
        // Stage 11: seed the current camera permission from its no-op provider.
        updateModel { copy(cameraPermission = permissions.status(Permission.Camera).name) }
        viewModelScope.launch { loadNotes() }
        viewModelScope.launch {
            val status = notifications.permissionStatus()
            updateModel { copy(notificationPermission = status.name) }
        }
    }

    private suspend fun loadNotes() {
        notes.all().fold(
            onSuccess = { stored -> updateModel { copy(notes = stored.map { it.content }) } },
            onFailure = { emit(DemoHomeEffect.Toast("Couldn't load notes: ${it.message}")) }
        )
    }

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
                // provider's hook (Sentry.init's own under sentryCrashReportingModule, CrashKiOS under
                // sentryCrashReportingModule) intercepts and forwards symbolicated — unlike
                // RecordTestCrash above, which is an explicitly-caught non-fatal. On Android the SDK's
                // own handler catches it. The demo's logging
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
            DemoHomeIntent.CapturePhoto ->
                camera.capturePhoto().fold(
                    onSuccess = { image -> updateModel { copy(cameraResult = "Captured ${image.bytes.size} bytes") } },
                    onFailure = { updateModel { copy(cameraResult = "No camera (no-op scaffold)") } }
                )

            DemoHomeIntent.RequestCameraPermission -> {
                val status = permissions.request(Permission.Camera)
                updateModel { copy(cameraPermission = status.name) }
            }

            DemoHomeIntent.ScheduleTrialReminder -> scheduleTrialReminder()

            DemoHomeIntent.CancelTrialReminder ->
                notifications.cancel(TRIAL_REMINDER_ID).fold(
                    onSuccess = { emit(DemoHomeEffect.Toast("Trial reminder cancelled")) },
                    onFailure = { emit(DemoHomeEffect.Toast("Cancel failed: ${it.message}")) }
                )
        }
    }

    /**
     * The host recipe for a trial-end reminder: read [EntitlementManager.status], ask for permission,
     * then schedule a local notification ahead of `expiresAt`. A real host would fire it a day or two
     * before expiry; the demo fires it [DEMO_REMINDER_DELAY] from now so it can be watched.
     */
    private suspend fun scheduleTrialReminder() {
        val status = entitlements.status.value
        val expiresAt = status.expiresAt
        if (expiresAt == null) {
            emit(DemoHomeEffect.Toast("Nothing expires — buy the Yearly (trial) or Monthly plan on the paywall first"))
            return
        }
        val permission = notifications.requestPermission()
        updateModel { copy(notificationPermission = permission.name) }
        if (permission != NotificationPermissionStatus.Granted) {
            emit(DemoHomeEffect.Toast("Notifications not allowed ($permission)"))
            return
        }
        // RevenueCat's Test Store products carry no trial phase, so the same reminder covers a
        // renewing subscription too; the copy follows isTrial / willRenew.
        val title =
            when {
                status.isTrial -> "Your free trial ends soon"
                status.willRenew -> "Your subscription renews soon"
                else -> "Your Pro access ends soon"
            }
        notifications
            .schedule(
                id = TRIAL_REMINDER_ID,
                at = Clock.System.now() + DEMO_REMINDER_DELAY,
                title = title,
                body = "Pro ends at $expiresAt. Cancel before then to avoid being charged."
            ).fold(
                onSuccess = { emit(DemoHomeEffect.Toast("Reminder in ${DEMO_REMINDER_DELAY.inWholeSeconds}s — background the app")) },
                onFailure = { emit(DemoHomeEffect.Toast("Couldn't schedule: ${it.message}")) }
            )
    }

    private companion object {
        const val TRIAL_REMINDER_ID = "trial_reminder"
        val DEMO_REMINDER_DELAY = 15.seconds
    }
}

// Pure model → UI mapping. (It previously ignored the model and returned a constant, so the Pro /
// god-mode labels never updated.)
private fun demoHomeScreenState(model: DemoHomeModelState): DemoHomeScreenState =
    DemoHomeScreenState(
        count = model.count,
        email = model.email,
        isPro = model.isPro,
        isGodMode = model.isGodMode,
        proSource = model.proSource,
        notes = model.notes,
        searchActive = model.searchActive,
        searchQuery = model.searchQuery,
        gallerySwitchOn = model.gallerySwitchOn,
        gallerySegmentIndex = model.gallerySegmentIndex,
        galleryNavIndex = model.galleryNavIndex,
        cameraResult = model.cameraResult,
        cameraPermission = model.cameraPermission,
        isTrial = model.isTrial,
        expiresAt = model.expiresAt,
        willRenew = model.willRenew,
        notificationPermission = model.notificationPermission,
        frnkHomeState =
            HomeScreenState(
                topBar =
                    FrnkTopAppBarState(
                        title = FrnkStringSource.Raw("Frnk")
                    )
            )
    )