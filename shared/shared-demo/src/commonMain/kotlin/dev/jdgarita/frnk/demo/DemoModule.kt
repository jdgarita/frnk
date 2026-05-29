package dev.jdgarita.frnk.demo

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.CrashReporter
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.database.Note
import dev.jdgarita.frnk.database.NoteStore
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.FeatureGate
import dev.jdgarita.frnk.ui.scaffolds.bottomNavScaffoldModule
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
 * `revenueCatModule` / `firebaseBackendModule` / `supabaseBackendModule` — the toolkit doesn't care.
 */
val demoModule =
    module {
        includes(onboardingScaffoldModule)
        includes(settingsScaffoldModule)
        includes(bottomNavScaffoldModule)
        // Bind the concrete fake so DemoViewModel can ask for it directly (for setPro()),
        // then expose it via the EntitlementManager interface as the same singleton.
        single { FakeEntitlementManager() }
        single<EntitlementManager> { get<FakeEntitlementManager>() }
        single<AnalyticsTracker> { LoggingAnalyticsTracker() }
        single<CrashReporter> { LoggingCrashReporter() }
        single { FeatureGate(get(), get()) }
        // In-memory NoteStore so the demo shows a persisted value (BACKLOG P1-1) without dragging
        // the SQLite native cinterop into DemoKit. The REAL SqlDelightNoteStore is bound by
        // databaseModule (via frnkModules) and covered by NoteStoreRoundTripTest.
        single<NoteStore> { FakeNoteStore() }
        viewModel { DemoViewModel(get(), get(), get(), get()) }
    }

/** In-memory Pro toggle so we can exercise FeatureGate without a paid SDK. */
class FakeEntitlementManager : EntitlementManager {
    private val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    fun setPro(value: Boolean) {
        _isPro.value = value
    }

    override suspend fun refresh() = Unit

    override suspend fun restorePurchases(): Boolean {
        _isPro.value = true
        return true
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
