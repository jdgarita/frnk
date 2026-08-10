package dev.jdgarita.frnk.monetization

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.CommonError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeatureGateTest {
    private fun gate(
        pro: Boolean = false,
        analytics: RecordingAnalytics = RecordingAnalytics(),
        freeFeatures: Set<Feature> = emptySet()
    ) = FeatureGate(FakeEntitlements(pro), analytics, freeFeatures)

    @Test
    fun pro_user_can_use_any_feature() {
        val g = gate(pro = true)
        assertTrue(g.canUse(FrnkFeature.Premium))
        assertTrue(g.canUse(FrnkFeature.AdFree))
    }

    @Test
    fun free_user_gated_unless_feature_is_free() {
        val g = gate(pro = false, freeFeatures = setOf(FrnkFeature.AdFree))
        assertTrue(g.canUse(FrnkFeature.AdFree))
        assertFalse(g.canUse(FrnkFeature.Premium))
    }

    @Test
    fun free_features_matched_by_id_across_implementations() {
        // freeFeatures is configured with the toolkit enum, but the gate is queried with a *different*
        // Feature implementation carrying the same id — the by-id comparison must still match.
        val g = gate(pro = false, freeFeatures = setOf(FrnkFeature.Premium))
        val hostPremium =
            object : Feature {
                override val id: String = "premium"
            }
        assertTrue(g.canUse(hostPremium))
    }

    @Test
    fun observe_reacts_to_entitlement_changes() =
        runTest {
            val entitlements = FakeEntitlements(pro = false)
            val g = FeatureGate(entitlements, RecordingAnalytics())
            assertFalse(g.observe(FrnkFeature.Premium).first())
            entitlements.setPro(true)
            assertTrue(g.observe(FrnkFeature.Premium).first())
        }

    @Test
    fun requestUpgrade_tracks_paywall_viewed_and_returns_route() {
        val analytics = RecordingAnalytics()
        val route = gate(analytics = analytics).requestUpgrade(source = "settings")
        assertEquals(FeatureGate.PAYWALL_ROUTE_KEY, route)
        assertTrue(analytics.tracked.contains(ToolkitEvent.PaywallViewed.key))
    }
}

private class FakeEntitlements(
    pro: Boolean
) : EntitlementManager {
    private val _isPro = MutableStateFlow(pro)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()
    override val status: StateFlow<EntitlementStatus> =
        MutableStateFlow(EntitlementStatus(pro, if (pro) ProSource.Purchase else ProSource.None)).asStateFlow()
    override val isGodMode: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()

    fun setPro(value: Boolean) {
        _isPro.value = value
    }

    override fun setGodMode(enabled: Boolean) = Unit

    override suspend fun refresh() = Unit

    override suspend fun identify(userId: String): AppResult<Unit, CommonError> = AppResult.Success(Unit)

    override suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError> = AppResult.Success(emptyList())

    override suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError> = AppResult.Success(true)

    override suspend fun restorePurchases(): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun syncPurchases(): AppResult<Boolean, MonetizationError> = AppResult.Success(false)

    override suspend fun managementUrl(): AppResult<String?, MonetizationError> = AppResult.Success(null)

    override suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError> = AppResult.Success(ProMetadata.DUMMY)
}

private class RecordingAnalytics : AnalyticsTracker {
    val tracked = mutableListOf<String>()

    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>
    ) {
        tracked += event.key
    }

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>
    ) = Unit

    override fun setUserProperty(
        key: String,
        value: String?
    ) = Unit
}