package dev.jdgarita.frnk.monetization

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * frnk-owned [EntitlementManager]: combines an [EntitlementProvider]'s purchased state with a persisted
 * god-mode override, and emits analytics user-properties so Pro/god-mode sessions are segmentable.
 *
 * - `isPro = provider.isPro || godMode`. God mode wins as the [ProSource].
 * - God mode is persisted via [KeyValueStore] so it survives restarts (usable in release builds).
 * - Every purchase/restore routes through here so the funnel analytics fire in one place.
 *
 * Lives for the app's lifetime (a Koin `single`), so its internal [scope] is never cancelled. Tests pass
 * a controlled scope.
 */
class DefaultEntitlementManager(
    private val provider: EntitlementProvider,
    private val keyValueStore: KeyValueStore,
    private val analytics: AnalyticsTracker,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : EntitlementManager {
    private val _isGodMode = MutableStateFlow(keyValueStore.getBoolean(GOD_MODE_KEY, default = false))
    override val isGodMode: StateFlow<Boolean> = _isGodMode.asStateFlow()

    private val _status = MutableStateFlow(compute())
    override val status: StateFlow<EntitlementStatus> = _status.asStateFlow()

    private val _isPro = MutableStateFlow(_status.value.isPro)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    init {
        // React to provider changes (StateFlow emits current immediately → seeds user properties once).
        provider.isPro.onEach { recompute() }.launchIn(scope)
    }

    override fun setGodMode(enabled: Boolean) {
        if (_isGodMode.value == enabled) return
        keyValueStore.putBoolean(GOD_MODE_KEY, enabled)
        _isGodMode.value = enabled
        analytics.trackCustom("god_mode_toggled", mapOf("enabled" to enabled))
        analytics.setUserProperty("god_mode", enabled.toString())
        recompute()
    }

    override suspend fun refresh() = provider.refresh()

    override suspend fun offerings(): AppResult<List<ProProduct>, MonetizationError> = provider.offerings()

    override suspend fun purchase(productId: String): AppResult<Boolean, MonetizationError> {
        analytics.track(ToolkitEvent.PurchaseStarted, mapOf("product" to productId))
        val result = provider.purchase(productId)
        when (result) {
            is AppResult.Success ->
                analytics.track(ToolkitEvent.PurchaseCompleted, mapOf("product" to productId))
            is AppResult.Failure ->
                analytics.track(
                    ToolkitEvent.PurchaseFailed,
                    mapOf("product" to productId, "error" to result.error.name),
                )
        }
        return result
    }

    override suspend fun restorePurchases(): AppResult<Boolean, MonetizationError> {
        analytics.trackCustom("restore_purchases")
        return provider.restore()
    }

    override suspend fun managementUrl(): AppResult<String?, MonetizationError> = provider.managementUrl()

    private fun compute(): EntitlementStatus {
        val god = _isGodMode.value
        val providerPro = provider.isPro.value
        val isPro = god || providerPro
        val source =
            when {
                god -> ProSource.GodMode
                providerPro -> ProSource.Purchase
                else -> ProSource.None
            }
        return EntitlementStatus(isPro = isPro, source = source)
    }

    private fun recompute() {
        val next = compute()
        if (next == _status.value && next.isPro == _isPro.value) return
        _status.value = next
        _isPro.value = next.isPro
        analytics.setUserProperty("is_pro", next.isPro.toString())
        analytics.setUserProperty("pro_source", next.source.name)
    }

    private companion object {
        const val GOD_MODE_KEY = "frnk.god_mode"
    }
}
