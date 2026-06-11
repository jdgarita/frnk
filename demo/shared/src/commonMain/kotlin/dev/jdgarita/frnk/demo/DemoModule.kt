package dev.jdgarita.frnk.demo

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.Note
import dev.jdgarita.frnk.database.NoteStore
import dev.jdgarita.frnk.monetization.EntitlementProvider
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProPlan
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.monetization.monetizationModule
import dev.jdgarita.frnk.monetization.ui.paywallScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.bottomNavScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.homeScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.onboardingScaffoldModule
import dev.jdgarita.frnk.ui.scaffolds.settingsScaffoldModule
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
 * `revenueCatModule` / `firebaseBackendModule` — the toolkit doesn't care.
 */
val demoModule =
    module {
        includes(homeScaffoldModule)
        includes(onboardingScaffoldModule)
        includes(settingsScaffoldModule)
        includes(bottomNavScaffoldModule)
        // The real frnk monetization layer (DefaultEntitlementManager + FeatureGate) over a FAKE
        // provider — so the demo exercises the actual god-mode / Free-Pro logic cross-platform without
        // a paid SDK. A real host installs `revenueCatModule` instead (androidDemoApp does).
        includes(monetizationModule)
        includes(paywallScaffoldModule)
        single<EntitlementProvider> { FakeEntitlementProvider() }
        // In-memory KeyValueStore so god mode persists for the session without the multiplatform-settings
        // impl; the real one is bound by databaseModule (via frnkModules).
        single<KeyValueStore> { FakeKeyValueStore() }
        single<AnalyticsTracker> { LoggingAnalyticsTracker() }
        single<CrashReporter> { LoggingCrashReporter() }
        // In-memory NoteStore so the demo shows a persisted value (BACKLOG P1-1) without dragging
        // the SQLite native cinterop into DemoKit. The REAL SqlDelightNoteStore is bound by
        // databaseModule (via frnkModules) and covered by NoteStoreRoundTripTest.
        single<NoteStore> { FakeNoteStore() }
        viewModel { DemoViewModel(get(), get(), get(), get(), get()) }
    }

/** In-memory [EntitlementProvider] so the demo exercises offerings + purchase/restore without a paid SDK. */
class FakeEntitlementProvider : EntitlementProvider {
    private val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    override suspend fun refresh() = Unit

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
                    badge = "Save 33%",
                ),
                ProProduct("lifetime", ProPlan.Lifetime, "Lifetime", "$99.99"),
            ),
        )

    override suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError> {
        _isPro.value = true
        return AppResult.Success(true)
    }

    override suspend fun restore(): AppResult<Boolean, MonetizationError> = AppResult.Success(_isPro.value)

    // The fake has no store-managed subscription, so there's nothing to open (real RC returns the
    // App Store / Play Store management URL after a real purchase).
    override suspend fun managementUrl(): AppResult<String?, MonetizationError> = AppResult.Success(null)
}

/** In-memory [KeyValueStore] for the demo (god-mode persistence) — keeps DemoKit free of the settings impl. */
class FakeKeyValueStore : KeyValueStore {
    private val strings = mutableMapOf<String, String>()
    private val booleans = mutableMapOf<String, Boolean>()

    override fun putString(
        key: String,
        value: String,
    ) {
        strings[key] = value
    }

    override fun getString(
        key: String,
        default: String?,
    ): String? = strings[key] ?: default

    override fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        booleans[key] = value
    }

    override fun getBoolean(
        key: String,
        default: Boolean,
    ): Boolean = booleans[key] ?: default

    override fun remove(key: String) {
        strings.remove(key)
        booleans.remove(key)
    }
}

/**
 * In-memory [NoteStore] for the demo — same role as [FakeEntitlementManager]: it lets the demo
 * exercise the persistence api surface without the SQLite native driver, keeping DemoKit
 * cinterop-free. The real relational path is [dev.jdgarita.frnk.database.impl.SqlDelightNoteStore].
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
        params: Map<String, Any?>,
    ) {
        PrintLogger.d(TAG, "${event.key} $params")
    }

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>,
    ) {
        PrintLogger.d(TAG, "$name $params")
    }

    override fun setUserProperty(
        key: String,
        value: String?,
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
        extras: Map<String, String>,
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
