package dev.jdgarita.frnk.demo

import dev.jdgarita.frnk.camera.cameraModule
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.demo.notes.Note
import dev.jdgarita.frnk.demo.notes.NoteStore
import dev.jdgarita.frnk.demo.ui.home.DemoHomeViewModel
import dev.jdgarita.frnk.identity.AnonymousIdentityProvider
import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.monetization.EntitlementProvider
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProMetadata
import dev.jdgarita.frnk.monetization.ProPlan
import dev.jdgarita.frnk.monetization.ProPrice
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.monetization.WebPurchaseRedemptionError
import dev.jdgarita.frnk.monetization.monetizationModule
import dev.jdgarita.frnk.monetization.ui.paywallScaffoldModule
import dev.jdgarita.frnk.permissions.permissionsModule
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.time.Clock

/**
 * The demo's host-side fakes, installed through `frnkModules { modules(frnkAppModule) }` in
 * [bootstrapDemoKoin] — the builder supplies the scaffold VMs, the mandatory PostHog + Sentry pair and
 * the no-op remote config; this module only binds what a real host would back with a paid SDK
 * (`revenueCatModule`, `prefsModule`, `databaseModule`). Observability is deliberately NOT faked
 * here: every host ships the real providers, the demo included.
 */
val frnkAppModule =
    module {
        // Stage 11 capability scaffolds — camera/permissions have no impl yet, so they stay no-op
        // everywhere (demoed as such).
        includes(cameraModule)
        includes(permissionsModule)
        // The real frnk monetization layer (DefaultEntitlementManager + FeatureGate) over a FAKE
        // provider — so the demo exercises the actual god-mode / Free-Pro logic cross-platform without
        // a paid SDK. A real host installs `revenueCatModule` instead (androidDemoApp does).
        includes(monetizationModule)
        includes(paywallScaffoldModule)
        single<EntitlementProvider> { FakeEntitlementProvider() }
        // In-memory identity so monetizationModule's SyncAuthUseCase stays resolvable; a real host
        // installs revenueCatIdentityModule (as demo-android does with a RevenueCat key) instead.
        single<AnonymousIdentityProvider> { FakeAnonymousIdentityProvider() }
        // In-memory KeyValueStore so god mode persists for the session without the
        // multiplatform-settings impl; a real host installs prefsModule (:data-prefs-impl) instead.
        single<KeyValueStore> { FakeKeyValueStore() }
        // In-memory NoteStore default so DemoKit/iOS stays free of the bundled SQLite driver.
        // androidDemoApp overrides it with the REAL path — databaseModule (:data-db-impl) +
        // demoNotesModule (demo-owned DemoDatabase over DatabaseFactory, OQ-2) — and the
        // Robolectric round-trip is covered by NoteStoreRoundTripTest.
        single<NoteStore> { FakeNoteStore() }
        viewModel { DemoHomeViewModel(get(), get(), get(), get(), get(), get(), get()) }
    }

/** In-memory [EntitlementProvider] so the demo exercises offerings + purchase/restore without a paid SDK. */
class FakeEntitlementProvider : EntitlementProvider {
    private val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    override suspend fun refresh() = Unit

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> = AppResult.Success(Unit)

    override suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError> =
        AppResult.Success(
            listOf(
                ProProduct(
                    "monthly",
                    ProPlan.Monthly,
                    "Monthly",
                    "$4.99",
                    pricePerMonthFormatted = "$4.99",
                    price = ProPrice(amountMicros = 4_990_000, currencyCode = "USD")
                ),
                ProProduct(
                    "yearly",
                    ProPlan.Yearly,
                    "Yearly",
                    "$39.99",
                    pricePerMonthFormatted = "$3.33",
                    hasFreeTrial = true,
                    badge = "Save 33%",
                    price = ProPrice(amountMicros = 39_990_000, currencyCode = "USD")
                ),
                ProProduct(
                    "lifetime",
                    ProPlan.Lifetime,
                    "Lifetime",
                    "$99.99",
                    price = ProPrice(amountMicros = 99_990_000, currencyCode = "USD")
                )
            )
        )

    override suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError> {
        _isPro.value = true
        return AppResult.Success(true)
    }

    override suspend fun restore(): AppResult<Boolean, MonetizationError> = AppResult.Success(_isPro.value)

    override suspend fun syncPurchases(): AppResult<Boolean, MonetizationError> = AppResult.Success(_isPro.value)

    // The fake has no store-managed subscription, so there's nothing to open (real RC returns the
    // App Store / Play Store management URL after a real purchase).
    override suspend fun managementUrl(): AppResult<String?, MonetizationError> = AppResult.Success(null)

    override suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError> = AppResult.Success(ProMetadata.DUMMY)

    override suspend fun redeemWebPurchase(url: String): AppResult<Boolean, WebPurchaseRedemptionError> =
        AppResult.Failure(WebPurchaseRedemptionError.NotARedemptionLink)
}

/** In-memory [AnonymousIdentityProvider] so the demo exercises the auth-sync path with no identity backend. */
class FakeAnonymousIdentityProvider : AnonymousIdentityProvider {
    private val _uid = MutableStateFlow<String?>(null)
    override val uid: StateFlow<String?> = _uid.asStateFlow()

    override suspend fun ensureSignedIn(): AppResult<String, CommonError> {
        _uid.value = DEMO_UID
        return AppResult.Success(DEMO_UID)
    }

    companion object {
        private const val DEMO_UID = "demo-anonymous-uid"
    }
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
 * exercise the persistence api surface without the bundled SQLite driver, keeping DemoKit
 * cinterop-free. The real relational path is `demoNotesModule`'s `RoomNoteStore`
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