package dev.jdgarita.frnk.monetization.ui

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Registers [PaywallViewModel]. The `source` is passed via `parametersOf(...)`; [EntitlementManager]
 * + [dev.jdgarita.frnk.backend.AnalyticsTracker] resolve from the graph (monetizationModule + observability).
 * Downstream Koin modules `includes(paywallScaffoldModule)`.
 */
val paywallScaffoldModule =
    module {
        viewModel { params -> PaywallViewModel(params.get(), get(), get()) }
    }
