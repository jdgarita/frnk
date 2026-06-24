package dev.jdgarita.frnk.ui.bottomnav

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin bindings for [FrnkNestedNavScaffold]'s [FrnkNestedNavViewModel].
 *
 * The VM takes no constructor params: the host's middle [FrnkCustomTab] arrives as
 * [FrnkNestedNavArguments] at attach time (via the scaffold's `RememberMviLifecycle`), keeping the host's
 * tab config at the composable boundary rather than in the DI graph. Installed by the core UI modules
 * (`frnkUiModules()` / `FrnkAppModule`).
 */
val frnkNestedNavModule =
    module {
        viewModel { FrnkNestedNavViewModel() }
    }