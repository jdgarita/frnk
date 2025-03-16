package dev.jdgarita.frnk.presentation.mvi.koin

import dev.jdgarita.frnk.presentation.mvi.ViewModelDependencies
import dev.jdgarita.frnk.util.di.KoinModuleProvider
import org.koin.core.module.Module
import org.koin.dsl.module

class CommonPresentationMviKoinModuleProvider : KoinModuleProvider {
    override val modules: List<Module>
        get() = listOf(
            module {
                factory {
                    ViewModelDependencies
                }
            }
        )
}