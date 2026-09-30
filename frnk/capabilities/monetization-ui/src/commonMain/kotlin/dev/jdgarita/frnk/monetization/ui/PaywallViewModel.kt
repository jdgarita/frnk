package dev.jdgarita.frnk.monetization.ui

import androidx.lifecycle.viewModelScope
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.monetization.MonetizationError
import dev.jdgarita.frnk.monetization.ProPlan
import dev.jdgarita.frnk.monetization.ProProduct
import dev.jdgarita.frnk.monetization.ui.ext.toStringSource
import dev.jdgarita.frnk.monetization.usecase.ObserveProStatusUseCase
import dev.jdgarita.frnk.monetization.usecase.PaywallPurchaseUseCase
import dev.jdgarita.frnk.monetization.usecase.SyncAuthUseCase
import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.stringPaywallAlreadyOwnedRestoring
import dev.jdgarita.frnk.ui.theme.stringPaywallIdentityError
import dev.jdgarita.frnk.ui.theme.stringPaywallNothingToRestore
import dev.jdgarita.frnk.ui.theme.stringPaywallPurchasePending
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
 * tags the analytics funnel (`Paywall_Viewed` / `Paywall_Dismissed`), and `dismissible = false` makes it a
 * hard paywall that ignores [PaywallIntent.Close].
 */
class PaywallViewModel(
    private val paywallPurchaseUseCase: PaywallPurchaseUseCase,
    private val analytics: AnalyticsTracker,
    private val syncAuthUseCase: SyncAuthUseCase,
    private val observeProStatus: ObserveProStatusUseCase? = null
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
    /** Hard mode: one Dismiss for Pro, whichever path (purchase, restore, sync, [observeProStatus]) gets there first. */
    private var dismissedForPro = false

    private val isHard: Boolean get() = !arguments.dismissible

    override fun onAttached(arguments: PaywallArguments) {
        analytics.track(ToolkitEvent.PaywallViewed, mapOf("source" to arguments.source))
        viewModelScope.launch { fetchPaywallData() }
        viewModelScope.launch { silentSync() }
        if (!arguments.dismissible) observeProStatus?.let { status -> viewModelScope.launch { dismissWhenPro(status) } }
    }

    /**
     * Hard mode: the paywall stands until the customer is Pro, whatever makes them Pro — an approved
     * pending purchase, a late receipt sync, a purchase on another device, god mode. While a purchase or
     * restore is in flight its own result decides (so [PaywallEffect.Purchased] precedes the Dismiss).
     */
    private suspend fun dismissWhenPro(status: ObserveProStatusUseCase) {
        status().collect { isPro ->
            val model = currentModel()
            if (isPro && !model.isPurchasing && !model.isRestoring) dismissForPro()
        }
    }

    /** Closes after a success. On a hard paywall at most once, since several paths can report the same Pro. */
    private suspend fun dismissForPro() {
        if (isHard) {
            if (dismissedForPro) return
            dismissedForPro = true
        }
        emit(PaywallEffect.Dismiss)
    }

    /** Hard mode, after a purchase / restore that did not close: Pro may have landed anyway (the listener). */
    private suspend fun dismissIfProArrived() {
        if (isHard && observeProStatus?.invoke()?.value == true) dismissForPro()
    }

    override suspend fun onIntent(intent: PaywallIntent) {
        when (intent) {
            is PaywallIntent.ProductSelected -> updateModel { copy(selectedProductId = intent.id) }
            PaywallIntent.Purchase -> purchase()
            PaywallIntent.Restore -> restore()
            PaywallIntent.Retry -> {
                updateModel { copy(isLoading = true) }
                fetchPaywallData()
            }
            PaywallIntent.Close -> {
                // A hard paywall only closes on success (purchase / restore / silent sync).
                if (!arguments.dismissible) return
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
                emit(PaywallEffect.Message(error.toStringSource()))
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
            dismissForPro()
        }
    }

    private suspend fun purchase() {
        val model = currentModel()
        val id = model.selectedProductId ?: return
        updateModel { copy(isPurchasing = true) }
        when (val result = paywallPurchaseUseCase.purchase(id)) {
            is AppResult.Success -> {
                // Only a purchase that activated the entitlement is a sale worth reporting; the
                // store accepting one it left pending still closes the sheet, silently.
                val product = model.products.firstOrNull { it.id == id }
                if (result.data && product != null) emit(PaywallEffect.Purchased(product))
                if (isHard && !result.data) {
                    // Pending (Ask to Buy, a slow payment method): not Pro yet, so a hard paywall stays up
                    // and closes through the Pro status once the store approves it.
                    updateModel { copy(isPurchasing = false) }
                    emit(PaywallEffect.Message(FrnkStringSource.Token(stringPaywallPurchasePending)))
                    dismissIfProArrived()
                } else {
                    dismissForPro() // manager flips status reactively
                }
            }

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

                    else -> emit(PaywallEffect.Message(result.error.toStringSource()))
                }
                dismissIfProArrived()
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
                    dismissForPro()
                } else {
                    emit(PaywallEffect.Message(FrnkStringSource.Token(stringPaywallNothingToRestore)))
                    dismissIfProArrived()
                }

            is AppResult.Failure -> {
                emit(PaywallEffect.Message(result.error.toStringSource()))
                dismissIfProArrived()
            }
        }
    }

    private fun defaultSelection(products: List<ProProduct>): String? =
        (products.firstOrNull { it.plan == ProPlan.Yearly } ?: products.firstOrNull())?.id
}