package dev.jdgarita.frnk.monetization

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultEntitlementManagerTest {
    private fun manager(
        provider: FakeProvider = FakeProvider(),
        kv: FakeKeyValueStore = FakeKeyValueStore(),
        analytics: FakeAnalytics = FakeAnalytics()
    ) = DefaultEntitlementManager(provider, kv, analytics, CoroutineScope(Dispatchers.Unconfined))

    @Test
    fun init_refreshes_provider_so_purchased_state_hydrates_on_cold_launch() {
        val provider = FakeProvider()
        manager(provider)
        // The Unconfined scope runs the construction-time launch eagerly.
        assertEquals(1, provider.refreshCount)
    }

    @Test
    fun god_mode_overlays_provider_and_sets_source() {
        val provider = FakeProvider()
        val mgr = manager(provider)
        assertFalse(mgr.isPro.value)
        assertEquals(ProSource.None, mgr.status.value.source)

        mgr.setGodMode(true)
        assertTrue(mgr.isPro.value)
        assertEquals(ProSource.GodMode, mgr.status.value.source)

        mgr.setGodMode(false)
        assertFalse(mgr.isPro.value)
        assertEquals(ProSource.None, mgr.status.value.source)
    }

    @Test
    fun provider_pro_reports_purchase_source() {
        val provider = FakeProvider()
        val mgr = manager(provider)
        provider.setPro(true)
        assertTrue(mgr.isPro.value)
        assertEquals(ProSource.Purchase, mgr.status.value.source)
    }

    @Test
    fun god_mode_wins_over_provider_for_source() {
        val provider = FakeProvider()
        val mgr = manager(provider)
        provider.setPro(true)
        mgr.setGodMode(true)
        assertEquals(ProSource.GodMode, mgr.status.value.source)
    }

    @Test
    fun god_mode_is_persisted_and_restored_on_init() {
        val kv = FakeKeyValueStore()
        manager(kv = kv).setGodMode(true)
        assertTrue(kv.getBoolean("frnk.god_mode"))

        // A fresh manager over the same store starts Pro via god mode.
        val restored = manager(kv = kv)
        assertTrue(restored.isPro.value)
        assertEquals(ProSource.GodMode, restored.status.value.source)
    }

    @Test
    fun free_at_startup_seeds_is_pro_and_pro_source_user_properties() {
        val analytics = FakeAnalytics()
        manager(analytics = analytics)
        // The reactive derivation must tag a plain Free session immediately, not only after a transition,
        // so god-mode/Pro sessions are filterable out of revenue analytics from the first event onward.
        assertEquals("false", analytics.userProperties["is_pro"])
        assertEquals(ProSource.None.name, analytics.userProperties["pro_source"])
    }

    @Test
    fun setGodMode_tracks_event_and_user_property() {
        val analytics = FakeAnalytics()
        manager(analytics = analytics).setGodMode(true)
        assertTrue(analytics.customEvents.any { it == "god_mode_toggled" })
        assertEquals("true", analytics.userProperties["god_mode"])
    }

    @Test
    fun purchase_emits_funnel_events() {
        val analytics = FakeAnalytics()
        val mgr = manager(provider = FakeProvider(purchaseResult = AppResult.Success(true)), analytics = analytics)
        // Use a tiny blocking bridge: run the suspend call on Unconfined via a manual loop.
        runTest {
            mgr.purchase("yearly")
        }
        assertTrue(analytics.tracked.contains(ToolkitEvent.PurchaseStarted.key))
        assertTrue(analytics.tracked.contains(ToolkitEvent.PurchaseCompleted.key))
    }

    @Test
    fun identify_delegates_to_provider_and_propagates_its_result() {
        val provider = FakeProvider(identifyResult = AppResult.Failure(IdentityError.Error))
        val mgr = manager(provider)
        runTest {
            assertEquals(AppResult.Failure(IdentityError.Error), mgr.identify("uid-1"))
        }
        assertEquals(listOf("uid-1"), provider.identifiedUserIds)
    }

    @Test
    fun syncPurchases_delegates_to_provider_and_tracks_event() {
        val provider = FakeProvider(syncResult = AppResult.Success(true))
        val analytics = FakeAnalytics()
        val mgr = manager(provider, analytics = analytics)
        runTest {
            assertEquals(AppResult.Success(true), mgr.syncPurchases())
        }
        assertEquals(1, provider.syncCount)
        assertTrue(analytics.customEvents.contains("sync_purchases"))
    }

    @Test
    fun purchase_failure_emits_failed_event() {
        val analytics = FakeAnalytics()
        val mgr =
            manager(
                provider = FakeProvider(purchaseResult = AppResult.Failure(MonetizationError.UserCancelled)),
                analytics = analytics
            )
        runTest { mgr.purchase("yearly") }
        assertTrue(analytics.tracked.contains(ToolkitEvent.PurchaseStarted.key))
        assertTrue(analytics.tracked.contains(ToolkitEvent.PurchaseFailed.key))
    }
}

private class FakeProvider(
    private val purchaseResult: AppResult<Boolean, MonetizationError> = AppResult.Success(true),
    private val identifyResult: AppResult<Unit, IdentityError> = AppResult.Success(Unit),
    private val syncResult: AppResult<Boolean, MonetizationError> = AppResult.Success(false)
) : EntitlementProvider {
    private val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    var refreshCount = 0
        private set

    var syncCount = 0
        private set

    val identifiedUserIds = mutableListOf<String>()

    fun setPro(value: Boolean) {
        _isPro.value = value
    }

    override suspend fun refresh() {
        refreshCount++
    }

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> {
        identifiedUserIds += id
        return identifyResult
    }

    override suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError> = AppResult.Success(emptyList())

    override suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError> {
        if (purchaseResult is AppResult.Success) _isPro.value = true
        return purchaseResult
    }

    override suspend fun restore(): AppResult<Boolean, MonetizationError> = AppResult.Success(_isPro.value)

    override suspend fun syncPurchases(): AppResult<Boolean, MonetizationError> {
        syncCount++
        return syncResult
    }

    override suspend fun managementUrl(): AppResult<String?, MonetizationError> = AppResult.Success(null)

    override suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError> = AppResult.Success(ProMetadata.DUMMY)
}

private class FakeKeyValueStore : KeyValueStore {
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

private class FakeAnalytics : AnalyticsTracker {
    val tracked = mutableListOf<String>()
    val customEvents = mutableListOf<String>()
    val userProperties = mutableMapOf<String, String?>()
    var identity: String? = null

    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>
    ) {
        tracked += event.key
    }

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>
    ) {
        customEvents += name
    }

    override fun setUserProperty(
        key: String,
        value: String?
    ) {
        userProperties[key] = value
    }

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> {
        identity = id
        return AppResult.Success(Unit)
    }
}