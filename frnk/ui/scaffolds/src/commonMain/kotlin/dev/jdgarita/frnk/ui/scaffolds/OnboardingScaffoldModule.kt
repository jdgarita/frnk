package dev.jdgarita.frnk.ui.scaffolds

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin bindings for the onboarding scaffold. Hosts that use the VM-backed [OnboardingScreen]
 * convenience composable must install this module (or `includes(onboardingScaffoldModule)` it
 * from a parent module).
 *
 * The ViewModel takes the initial [OnboardingScreenState] as a runtime parameter, so the call site
 * passes the page list via `parametersOf(initialState)` — this keeps the page catalog at the
 * composable boundary rather than in the DI graph.
 */
val onboardingScaffoldModule =
    module {
        viewModel { params -> OnboardingViewModel(initial = params.get()) }
    }
