package dev.jdgarita.frnk.monetization.ui

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.ui.mvi.Arguments
import dev.jdgarita.frnk.ui.mvi.ModelState
import dev.jdgarita.frnk.ui.mvi.ModelStateFactory
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState

/**
 * The data-only layer behind [PaywallScreenState]: the loaded offerings, the current selection, and
 * the in-flight/purchase flags. [PaywallViewModel] mutates this; the UI state is derived from it.
 */
data class PaywallModelState(
    val products: List<ProProduct> = emptyList(),
    val selectedProductId: String? = null,
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false
) : ModelState

/** Seeds the paywall's initial model: empty + loading (offerings are fetched on first composition). */
object PaywallModelStateFactory : ModelStateFactory<PaywallModelState> {
    override fun initialModelState() = PaywallModelState()
}

/**
 * Runtime input for the paywall, supplied at attach time. Holds only the analytics [source] (where the
 * paywall was opened from: `home_topbar`, `settings`, `feature_gate:<id>`) — service deps stay in the VM
 * constructor.
 */
data class PaywallArguments(
    val source: String
) : Arguments

/**
 * Dynamic state for the toolkit paywall. Static config (app name, feature bullets) is passed to the
 * stateless content directly; this holds the offerings, selection, and in-flight/purchase state.
 *
 * Derived from [PaywallModelState] by [PaywallViewModel]; not built directly by callers.
 */
@Immutable
data class PaywallScreenState(
    val products: List<ProProduct> = emptyList(),
    val selectedProductId: String? = null,
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false
) : UiState {
    val selectedProduct: ProProduct? get() = products.firstOrNull { it.id == selectedProductId }
}

sealed interface PaywallIntent : UiIntent {
    data class ProductSelected(
        val id: String
    ) : PaywallIntent

    data object Purchase : PaywallIntent

    data object Restore : PaywallIntent

    data object Close : PaywallIntent
}

sealed interface PaywallEffect : UiEffect {
    /** Close the paywall (purchase/restore succeeded, or the user dismissed it). */
    data object Dismiss : PaywallEffect

    /** Show a transient message (purchase failed/cancelled, nothing to restore). */
    data class Message(
        val text: String
    ) : PaywallEffect
}