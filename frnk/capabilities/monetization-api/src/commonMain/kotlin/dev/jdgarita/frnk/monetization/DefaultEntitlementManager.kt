package dev.jdgarita.frnk.monetization

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.database.KeyValueStore
import dev.jdgarita.frnk.database.booleanPreference
import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * frnk-owned [EntitlementManager]: combines an [EntitlementProvider]'s purchased state with a persisted
 * god-mode override, and emits analytics user-properties so Pro/god-mode sessions are segmentable.
 *
 * - `isPro = provider.isPro || godMode`. God mode wins as the [ProSource].
 * - God mode is persisted via [KeyValueStore] so it survives restarts (usable in release builds).
 * - Every purchase/restore routes through here so the funnel analytics fire in one place.
 * - Construction kicks off `provider.refresh()` so purchased state hydrates on cold launch —
 *   at bootstrap when `validateFrnkBootstrap` resolves the manager, else on first injection.
 *
 * Lives for the app's lifetime (a Koin `single`). Its application-owned [scope] is injected and
 * cancelled when the Koin application closes. Tests pass a controlled scope.
 */
class DefaultEntitlementManager(
    private val provider: EntitlementProvider,
    private val keyValueStore: KeyValueStore,
    private val analytics: AnalyticsTracker,
    scope: CoroutineScope
) : EntitlementManager {
    /** Typed view over the persisted god-mode flag — same key/representation as a raw `putBoolean`. */
    private val godModePref = keyValueStore.booleanPreference(GOD_MODE_KEY, default = false)
    private val _isGodMode = MutableStateFlow(godModePref.value)
    override val isGodMode: StateFlow<Boolean> = _isGodMode.asStateFlow()

    /**
     * Single reactive derivation of `provider.isPro × godMode`. Because `status`/`isPro` are *derived*
     * (never written by hand) there is no read-modify-write race between [setGodMode] and the provider's
     * background pushes — `combine` is the only writer. The `onEach` re-seeds the analytics user-properties
     * on every distinct status, including the very first emission, so a Free-at-startup session is still
     * tagged `is_pro=false` / `pro_source=None`.
     */
    override val status: StateFlow<EntitlementStatus> =
        combine(provider.isPro, _isGodMode) { providerPro, god -> compute(providerPro, god) }
            .distinctUntilChanged()
            .onEach { next ->
                analytics.setUserProperty("is_pro", next.isPro.toString())
                analytics.setUserProperty("pro_source", next.source.name)
            }.stateIn(scope, SharingStarted.Eagerly, compute(provider.isPro.value, _isGodMode.value))

    override val isPro: StateFlow<Boolean> =
        status.map { it.isPro }.stateIn(scope, SharingStarted.Eagerly, status.value.isPro)

    init {
        // Hydrate purchased state at construction: without this, a provider whose `isPro` starts
        // false (RevenueCat answers from its local cache, even offline) would report Free on every
        // cold launch until some paywall interaction happened to trigger a customer-info fetch.
        scope.launch { provider.refresh() }
    }

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> = provider.identify(id = id)

    override fun setGodMode(enabled: Boolean) {
        if (_isGodMode.value == enabled) return
        godModePref.value = enabled
        analytics.trackCustom("god_mode_toggled", mapOf("enabled" to enabled))
        analytics.setUserProperty("god_mode", enabled.toString())
        // The combine derivation reacts to this and recomputes status/isPro + their user-properties.
        _isGodMode.value = enabled
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
                    mapOf("product" to productId, "error" to result.error.name)
                )
        }
        return result
    }

    override suspend fun restorePurchases(): AppResult<Boolean, MonetizationError> {
        analytics.trackCustom("restore_purchases")
        return provider.restore()
    }

    override suspend fun syncPurchases(): AppResult<Boolean, MonetizationError> {
        analytics.trackCustom("sync_purchases")
        return provider.syncPurchases()
    }

    override suspend fun managementUrl(): AppResult<String?, MonetizationError> = provider.managementUrl()

    override suspend fun fetchMetadata(): AppResult<ProMetadata, MonetizationError> = provider.fetchMetadata()

    private fun compute(
        providerPro: Boolean,
        god: Boolean
    ): EntitlementStatus {
        val source =
            when {
                god -> ProSource.GodMode
                providerPro -> ProSource.Purchase
                else -> ProSource.None
            }
        return EntitlementStatus(isPro = god || providerPro, source = source)
    }

    private companion object {
        const val GOD_MODE_KEY = "frnk.god_mode"
    }
}