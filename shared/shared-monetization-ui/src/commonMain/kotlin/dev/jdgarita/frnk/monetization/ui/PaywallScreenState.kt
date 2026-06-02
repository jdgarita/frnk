package dev.jdgarita.frnk.monetization.ui

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState

/**
 * Dynamic state for the toolkit paywall. Static config (app name, feature bullets) is passed to the
 * stateless content directly; this holds the offerings, selection, and in-flight/purchase state.
 */
@Immutable
data class PaywallScreenState(
    val products: List<ProProduct> = emptyList(),
    val selectedProductId: String? = null,
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
) : UiState {
    val selectedProduct: ProProduct? get() = products.firstOrNull { it.id == selectedProductId }
}

sealed interface PaywallIntent : UiIntent {
    data class ProductSelected(
        val id: String,
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
        val text: String,
    ) : PaywallEffect
}
