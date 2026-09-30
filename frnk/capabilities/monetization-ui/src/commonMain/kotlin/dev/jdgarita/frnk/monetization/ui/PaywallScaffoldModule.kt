package dev.jdgarita.frnk.monetization.ui

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Registers [PaywallViewModel]. [dev.jdgarita.frnk.monetization.usecase.PaywallPurchaseUseCase] +
 * [dev.jdgarita.frnk.backend.AnalyticsTracker] +
 * [dev.jdgarita.frnk.monetization.usecase.SyncAuthUseCase] resolve from the graph
 * (monetizationModule + observability), plus `ObserveProStatusUseCase` when bound (a hard paywall closes on
 * Pro through it); the `source` is supplied at attach time via
 * `PaywallArguments`, not through Koin. Downstream Koin modules `includes(paywallScaffoldModule)`.
 */
val paywallScaffoldModule =
    module {
        viewModel {
            PaywallViewModel(
                paywallPurchaseUseCase = get(),
                analytics = get(),
                syncAuthUseCase = get(),
                // Optional so a graph without monetizationModule's binding still builds; a hard paywall then
                // closes only through purchase / restore / sync results.
                observeProStatus = getOrNull()
            )
        }
    }