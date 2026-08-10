package dev.jdgarita.frnk.monetization.ui

import androidx.lifecycle.viewModelScope
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProPlan
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.monetization.usecase.PaywallPurchaseUseCase
import dev.jdgarita.frnk.monetization.usecase.SyncAuthUseCase
import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.stringPaywallAlreadyOwnedRestoring
import dev.jdgarita.frnk.ui.theme.stringPaywallIdentityError
import dev.jdgarita.frnk.ui.theme.stringPaywallNothingToRestore
import dev.jdgarita.frnk.ui.theme.stringPaywallRestored
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Drives the toolkit paywall: loads metadata + offerings in parallel (committed atomically — either
 * both apply or a single error message is surfaced), tracks the funnel, and runs purchase/restore through
 * the injectable [PaywallPurchaseUseCase] (so the ViewModel stays agnostic of the concrete
 * `EntitlementManager`). Success closes the paywall ([PaywallEffect.Dismiss]); cancel / failure
 * surface a [PaywallEffect.Message] — nothing throws.
 *
 * Restore correctness: every store interaction is sequenced behind [SyncAuthUseCase.identify] so the
 * entitlement lands on the host's stable uid, never RC's transient anonymous id. On attach the paywall
 * also runs a best-effort **silent receipt sync** — a reinstalled Pro user is dismissed with their Pro
 * restored instead of being sold to; and a purchase that fails with [MonetizationError.AlreadyOwned]
 * falls through to a restore automatically instead of dead-ending on an error dialog.
 *
 * Owns a [PaywallModelState] (the data) and maps it to [PaywallScreenState] (the rendered state).
 * Runtime input arrives as [PaywallArguments] at attach time (see [onAttached]); the `source` they carry
 * tags the analytics funnel (`Paywall_Viewed` / `Paywall_Dismissed`).
 */
class PaywallViewModel(
    private val paywallPurchaseUseCase: PaywallPurchaseUseCase,
    private val analytics: AnalyticsTracker,
    private val syncAuthUseCase: SyncAuthUseCase
) : MviViewModel<PaywallArguments, PaywallModelState, PaywallScreenState, PaywallIntent, PaywallEffect>(
        factory = PaywallModelStateFactory,
        mapper = { modelState ->
            PaywallScreenState(
                title = modelState.title,
                subtitle = modelState.subtitle,
                products = modelState.products,
                benefits = modelState.benefits,
                selectedProductId = modelState.selectedProductId,
                isLoading = modelState.isLoading,
                isPurchasing = modelState.isPurchasing,
                isRestoring = modelState.isRestoring
            )
        }
    ) {
    override fun onAttached(arguments: PaywallArguments) {
        analytics.track(ToolkitEvent.PaywallViewed, mapOf("source" to arguments.source))
        viewModelScope.launch { fetchPaywallData() }
        viewModelScope.launch { silentSync() }
    }

    override suspend fun onIntent(intent: PaywallIntent) {
        when (intent) {
            is PaywallIntent.ProductSelected -> updateModel { copy(selectedProductId = intent.id) }
            PaywallIntent.Purchase -> purchase()
            PaywallIntent.Restore -> restore()
            PaywallIntent.Close -> {
                analytics.track(ToolkitEvent.PaywallDismissed, mapOf("source" to arguments.source))
                emit(PaywallEffect.Dismiss)
            }
        }
    }

    private suspend fun fetchPaywallData() =
        coroutineScope {
            val metadataDeferred = async { paywallPurchaseUseCase.fetchMetadata() }
            val productsDeferred = async { paywallPurchaseUseCase.offerings() }
            val metadata = metadataDeferred.await()
            val products = productsDeferred.await()

            if (metadata is AppResult.Success && products is AppResult.Success) {
                updateModel {
                    copy(
                        title = metadata.data.title,
                        subtitle = metadata.data.subtitle,
                        benefits = metadata.data.benefits,
                        products = products.data,
                        selectedProductId = defaultSelection(products.data),
                        isLoading = false
                    )
                }
            } else {
                updateModel { copy(isLoading = false) }
                val error =
                    (metadata as? AppResult.Failure)?.error
                        ?: (products as AppResult.Failure).error
                emit(PaywallEffect.Message(FrnkStringSource.Raw(error.message)))
            }
        }

    /**
     * Best-effort receipt sync before selling: a reinstalled Pro user (new host uid) is recovered
     * silently and never sees the sell screen. Failures are silent — the paywall still renders.
     */
    private suspend fun silentSync() {
        if (syncAuthUseCase.identify() is AppResult.Failure) return
        val result = paywallPurchaseUseCase.sync()
        if (result is AppResult.Success && result.data) {
            emit(PaywallEffect.Message(FrnkStringSource.Token(stringPaywallRestored)))
            emit(PaywallEffect.Dismiss)
        }
    }

    private suspend fun purchase() {
        val id = currentModel().selectedProductId ?: return
        updateModel { copy(isPurchasing = true) }
        when (val result = paywallPurchaseUseCase.purchase(id)) {
            is AppResult.Success -> emit(PaywallEffect.Dismiss) // manager flips status reactively
            is AppResult.Failure -> {
                updateModel { copy(isPurchasing = false) }
                when (result.error) {
                    MonetizationError.UserCancelled -> Unit
                    // The store says the user already owns this (Android surfaces it as an error
                    // instead of self-healing like iOS) — recover by restoring instead of dead-ending.
                    MonetizationError.AlreadyOwned -> {
                        emit(PaywallEffect.Message(FrnkStringSource.Token(stringPaywallAlreadyOwnedRestoring)))
                        restore()
                    }

                    else -> emit(PaywallEffect.Message(FrnkStringSource.Raw(result.error.message)))
                }
            }
        }
    }

    private suspend fun restore() {
        updateModel { copy(isRestoring = true) }
        // Restore attaches the receipt to the *current* billing identity — gate on the identity sync
        // so it never lands on RC's transient anonymous id (the host backend looks up by host uid).
        if (syncAuthUseCase.identify() is AppResult.Failure) {
            updateModel { copy(isRestoring = false) }
            emit(PaywallEffect.Message(FrnkStringSource.Token(stringPaywallIdentityError)))
            return
        }
        val result = paywallPurchaseUseCase.restore()
        updateModel { copy(isRestoring = false) }
        when (result) {
            is AppResult.Success ->
                if (result.data) {
                    emit(PaywallEffect.Dismiss)
                } else {
                    emit(PaywallEffect.Message(FrnkStringSource.Token(stringPaywallNothingToRestore)))
                }

            is AppResult.Failure -> emit(PaywallEffect.Message(FrnkStringSource.Raw(result.error.message)))
        }
    }

    private fun defaultSelection(products: List<ProProduct>): String? =
        (products.firstOrNull { it.plan == ProPlan.Yearly } ?: products.firstOrNull())?.id
}