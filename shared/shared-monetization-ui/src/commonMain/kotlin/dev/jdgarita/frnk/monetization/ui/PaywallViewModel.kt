package dev.jdgarita.frnk.monetization.ui

import androidx.lifecycle.viewModelScope
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProPlan
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.launch

/**
 * Drives the toolkit paywall: loads offerings, tracks the funnel, and runs purchase/restore through
 * the frnk-owned [EntitlementManager]. Success closes the paywall ([PaywallEffect.Dismiss]); cancel /
 * failure surface a [PaywallEffect.Message] — nothing throws.
 *
 * @param source where the paywall was opened from (`home_topbar`, `settings`, `feature_gate:<id>`).
 */
class PaywallViewModel(
    private val source: String,
    private val entitlements: EntitlementManager,
    private val analytics: AnalyticsTracker,
) : MviViewModel<PaywallScreenState, PaywallIntent, PaywallEffect>(PaywallScreenState()) {
    init {
        analytics.track(ToolkitEvent.PaywallViewed, mapOf("source" to source))
        viewModelScope.launch { loadOfferings() }
    }

    override suspend fun onIntent(intent: PaywallIntent) {
        when (intent) {
            is PaywallIntent.ProductSelected -> setState { copy(selectedProductId = intent.id) }
            PaywallIntent.Purchase -> purchase()
            PaywallIntent.Restore -> restore()
            PaywallIntent.Close -> {
                analytics.track(ToolkitEvent.PaywallDismissed, mapOf("source" to source))
                emit(PaywallEffect.Dismiss)
            }
        }
    }

    private suspend fun loadOfferings() {
        when (val result = entitlements.offerings()) {
            is AppResult.Success ->
                setState {
                    copy(
                        products = result.data,
                        selectedProductId = defaultSelection(result.data),
                        isLoading = false,
                    )
                }
            is AppResult.Failure -> {
                setState { copy(isLoading = false) }
                emit(PaywallEffect.Message(result.error.message))
            }
        }
    }

    private suspend fun purchase() {
        val id = currentState().selectedProductId ?: return
        setState { copy(isPurchasing = true) }
        when (val result = entitlements.purchase(id)) {
            is AppResult.Success -> emit(PaywallEffect.Dismiss) // manager flips status reactively
            is AppResult.Failure -> {
                setState { copy(isPurchasing = false) }
                if (result.error != MonetizationError.UserCancelled) {
                    emit(PaywallEffect.Message(result.error.message))
                }
            }
        }
    }

    private suspend fun restore() {
        when (val result = entitlements.restorePurchases()) {
            is AppResult.Success ->
                if (result.data) emit(PaywallEffect.Dismiss) else emit(PaywallEffect.Message("Nothing to restore"))
            is AppResult.Failure -> emit(PaywallEffect.Message(result.error.message))
        }
    }

    private fun defaultSelection(products: List<ProProduct>): String? =
        (products.firstOrNull { it.plan == ProPlan.Yearly } ?: products.firstOrNull())?.id
}
