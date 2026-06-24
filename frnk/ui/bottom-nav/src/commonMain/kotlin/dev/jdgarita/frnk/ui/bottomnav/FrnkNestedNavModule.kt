package dev.jdgarita.frnk.ui.bottomnav

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin bindings for the onboarding scaffold. Hosts that use the VM-backed [FrnkOnboardingScreen]
 * convenience composable must install this module (or `includes(onboardingScaffoldModule)` it
 * from a parent module).
 *
 * The ViewModel takes no constructor params: the page catalog arrives as [OnboardingArguments] at
 * attach time (via [FrnkOnboardingScreen]'s [dev.jdgarita.frnk.ui.mvi.FrnkScreen] wrapper), keeping it
 * at the composable boundary rather than in the DI graph.
 */
val frnkNestedNavModule =
    module {
        viewModel { FrnkNestedNavViewModel() }
    }