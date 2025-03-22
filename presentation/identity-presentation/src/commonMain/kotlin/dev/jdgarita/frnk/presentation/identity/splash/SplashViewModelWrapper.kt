package dev.jdgarita.frnk.presentation.identity.splash

import dev.jdgarita.frnk.presentation.mvi.MviViewModelWrapper

class SplashViewModelWrapper(viewModel: SplashViewModel) :
    MviViewModelWrapper<SplashArguments, SplashIntent, SplashViewState, SplashExternalEvent>(
        model = viewModel
    ),
    SplashViewModel