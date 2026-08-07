package dev.jdgarita.frnk.demo

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.camera.cameraModule
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.demo.notes.Note
import dev.jdgarita.frnk.demo.notes.NoteStore
import dev.jdgarita.frnk.demo.ui.home.DemoHomeViewModel
import dev.jdgarita.frnk.monetization.EntitlementProvider
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProMetadata
import dev.jdgarita.frnk.monetization.ProPlan
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.monetization.monetizationModule
import dev.jdgarita.frnk.monetization.ui.paywallScaffoldModule
import dev.jdgarita.frnk.permissions.permissionsModule
import dev.jdgarita.frnk.remoteconfig.noopRemoteConfigModule
import dev.jdgarita.frnk.ui.app.appearanceModule
import dev.jdgarita.frnk.ui.bottomnav.frnkNestedNavModule
import dev.jdgarita.frnk.ui.scaffolds.home.homeScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.onboarding.onboardingScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.settings.settingsScaffoldModule
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import dev.jdgarita.frnk.utils.PrintLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.time.Clock

/**
 * Demo wiring. The point of having a separate module is that a real host would swap these for
 * `revenueCatModule` / `firebaseObservabilityModule` — the toolkit doesn't care.
 */
val frnkAppModule =
    module {
        includes(appearanceModule)
        includes(homeScaffoldModule)
        includes(onboardingScaffoldModule)
        includes(frnkNestedNavModule)
        includes(settingsScaffoldModule)
        // Stage 11 capability scaffolds — all no-op api defaults so DemoKit's common surface stays
        // SDK-free. androidDemoApp overrides remoteConfig with the real Firebase remoteConfigModule;
        // camera/permissions have no impl yet, so they stay no-op everywhere (demoed as such).
        includes(noopRemoteConfigModule)
        includes(cameraModule)
        includes(permissionsModule)
        // The real frnk monetization layer (DefaultEntitlementManager + FeatureGate) over a FAKE
        // provider — so the demo exercises the actual god-mode / Free-Pro logic cross-platform without
        // a paid SDK. A real host installs `revenueCatModule` instead (androidDemoApp does).
        includes(monetizationModule)
        includes(paywallScaffoldModule)
        single<EntitlementProvider> { FakeEntitlementProvider() }
        // In-memory KeyValueStore so god mode persists for the session without the
        // multiplatform-settings impl; a real host installs prefsModule (:data-prefs-impl) instead.
        single<KeyValueStore> { FakeKeyValueStore() }
        single<AnalyticsTracker> { LoggingAnalyticsTracker() }
        single<CrashReporter> { LoggingCrashReporter() }
        // In-memory NoteStore default so DemoKit/iOS stays free of the SQLite driver.
        // androidDemoApp overrides it with the REAL path — databaseModule (:data-db-impl) +
        // demoNotesModule (demo-owned DemoDB over SqlDriverFactory, OQ-2) — and the JVM
        // round-trip is covered by NoteStoreRoundTripTest.
        single<NoteStore> { FakeNoteStore() }
        viewModel { DemoHomeViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    }

/** In-memory [EntitlementProvider] so the demo exercises offerings + purchase/restore without a paid SDK. */
class FakeEntitlementProvider : EntitlementProvider {
    private val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    override suspend fun refresh() = Unit

    override suspend fun identify(userId: String): AppResult<Unit, CommonError> = AppResult.Success(Unit)

    override suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError> =
        AppResult.Success(
            listOf(
                ProProduct("monthly", ProPlan.Monthly, "Monthly", "$4.99", pricePerMonthFormatted = "$4.99"),
                ProProduct(
                    "yearly",
                    ProPlan.Yearly,
                    "Yearly",
                    "$39.99",
                    pricePerMonthFormatted = "$3.33",
                    hasFreeTrial = true,
                    badge = "Save 33%"
                ),
                ProProduct("lifetime", ProPlan.Lifetime, "Lifetime", "$99.99")
            )
        )

    override suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError> {
        _isPro.value = true
        return AppResult.Success(true)
    }

    override suspend fun restore(): AppResult<Boolean, MonetizationError> = AppResult.Success(_isPro.value)

    // The fake has no store-managed subscription, so there's nothing to open (real RC returns the
    // App Store / Play Store management URL after a real purchase).
    override suspend fun managementUrl(): AppResult<String?, MonetizationError> = AppResult.Success(null)

    override suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError> = AppResult.Success(ProMetadata.DUMMY)
}

/** In-memory [KeyValueStore] for the demo (god-mode persistence) — keeps DemoKit free of the settings impl. */
class FakeKeyValueStore : KeyValueStore {
    private val strings = mutableMapOf<String, String>()
    private val booleans = mutableMapOf<String, Boolean>()

    override fun putString(
        key: String,
        value: String
    ) {
        strings[key] = value
    }

    override fun getString(
        key: String,
        default: String?
    ): String? = strings[key] ?: default

    override fun putBoolean(
        key: String,
        value: Boolean
    ) {
        booleans[key] = value
    }

    override fun getBoolean(
        key: String,
        default: Boolean
    ): Boolean = booleans[key] ?: default

    override fun remove(key: String) {
        strings.remove(key)
        booleans.remove(key)
    }
}

/**
 * In-memory [NoteStore] for the demo — same role as [FakeEntitlementProvider]: it lets the demo
 * exercise the persistence api surface without the SQLite native driver, keeping DemoKit
 * cinterop-free. The real relational path is `demoNotesModule`'s `SqlDelightNoteStore`
 * (`dev.jdgarita.frnk.demo.notes`), which androidDemoApp installs over this.
 */
class FakeNoteStore : NoteStore {
    private val notes = mutableListOf<Note>()
    private var nextId = 1L

    override suspend fun add(content: String): AppResult<Note, CommonError> {
        val note = Note(id = nextId++, content = content, createdAt = Clock.System.now())
        notes.add(0, note)
        return AppResult.Success(note)
    }

    override suspend fun all(): AppResult<List<Note>, CommonError> = AppResult.Success(notes.toList())

    override suspend fun clear(): AppResult<Unit, CommonError> {
        notes.clear()
        return AppResult.Success(Unit)
    }
}

class LoggingAnalyticsTracker : AnalyticsTracker {
    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>
    ) {
        PrintLogger.d(TAG, "${event.key} $params")
    }

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>
    ) {
        PrintLogger.d(TAG, "$name $params")
    }

    override fun setUserProperty(
        key: String,
        value: String?
    ) {
        PrintLogger.d(TAG, "user[$key] = $value")
    }

    companion object {
        private const val TAG = "Analytics"
    }
}

class LoggingCrashReporter : CrashReporter {
    override fun recordException(
        throwable: Throwable,
        extras: Map<String, String>
    ) {
        PrintLogger.e(TAG, "$extras", throwable)
    }

    override fun setUserId(id: String?) {
        PrintLogger.d(TAG, "userId=$id")
    }

    override fun log(message: String) {
        PrintLogger.d(TAG, message)
    }

    companion object {
        private const val TAG = "Crash"
    }
}