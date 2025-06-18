package dev.jdgarita.frnk.presentation.screen.home.di

import dev.jdgarita.frnk.presentation.screen.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 19/02/2024
 */

val homeModule = module {

    viewModelOf(::HomeViewModel)
}
