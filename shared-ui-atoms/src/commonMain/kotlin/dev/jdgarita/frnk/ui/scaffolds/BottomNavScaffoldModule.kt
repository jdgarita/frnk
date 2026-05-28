package dev.jdgarita.frnk.ui.scaffolds

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin bindings for the bottom-nav scaffold. Hosts that use the VM-backed `BottomNavScaffold`
 * convenience composable must install this module (or `includes(bottomNavScaffoldModule)` it from a
 * parent module).
 *
 * The ViewModel takes the initial [BottomNavScaffoldState] as a runtime parameter, so the call site
 * passes the tab list via `parametersOf(initialState)` — this keeps the tab catalog at the
 * composable boundary rather than in the DI graph.
 */
val bottomNavScaffoldModule =
    module {
        viewModel { params -> BottomNavViewModel(initial = params.get()) }
    }
