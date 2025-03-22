package dev.jdgarita.frnk.presentation.identity.koin

import dev.jdgarita.frnk.presentation.identity.splash.SplashViewModel
import dev.jdgarita.frnk.presentation.identity.splash.SplashViewModelWrapper
import dev.jdgarita.frnk.util.di.KoinModuleProvider
import dev.jdgarita.frnk.presentation.identity.splash.DefaultSplashViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class CommonIdentityPresentationKoinModuleProvider : KoinModuleProvider {

    override val modules: List<Module>
        get() = listOf(
            module {
                factory<SplashViewModel> {
                    DefaultSplashViewModel(
                        viewModelDependencies = get()
                    )
                }

                viewModel { SplashViewModelWrapper(viewModel = get()) }
            }
        )
}