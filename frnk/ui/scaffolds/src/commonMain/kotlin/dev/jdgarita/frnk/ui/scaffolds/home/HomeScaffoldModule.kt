package dev.jdgarita.frnk.ui.scaffolds.home

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin bindings for the home scaffold. Hosts that use the VM-backed [HomeScreen] convenience
 * composable must install this module (or `includes(homeScaffoldModule)` it from a parent module);
 * `frnkUiModules()` in `:ui-app` includes it.
 *
 * The ViewModel takes the initial [HomeScreenState] as a runtime parameter, so the call site passes
 * the configured chrome via `parametersOf(initialState)` — mirrors [dev.jdgarita.frnk.ui.scaffolds.settings.settingsScaffoldModule].
 */
val homeScaffoldModule =
    module {
        viewModel { params -> HomeViewModel(initial = params.get()) }
    }
