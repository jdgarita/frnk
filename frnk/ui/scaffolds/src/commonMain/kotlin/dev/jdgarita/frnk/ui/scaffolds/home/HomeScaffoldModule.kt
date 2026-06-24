package dev.jdgarita.frnk.ui.scaffolds.home

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin bindings for the home scaffold. Hosts that use the VM-backed [HomeScreen] convenience
 * composable must install this module (or `includes(homeScaffoldModule)` it from a parent module);
 * `frnkUiModules()` in `:ui-app` includes it.
 *
 * The ViewModel is model-first: runtime chrome (the top-bar title) arrives as [HomeArguments] at
 * attach time via [dev.jdgarita.frnk.ui.mvi.FrnkScreen], not through the constructor — so the binding
 * just resolves the [dev.jdgarita.frnk.monetization.usecase.ObserveProStatusUseCase] it needs.
 * Mirrors [dev.jdgarita.frnk.ui.scaffolds.settings.settingsScaffoldModule].
 */
val homeScaffoldModule =
    module {
        viewModel { HomeViewModel(observeProStatus = get()) }
    }