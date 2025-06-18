package dev.jdgarita.frnk.presentation.screen.detail.di

import dev.jdgarita.frnk.presentation.screen.detail.DetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 19/02/2024
 */

val detailModule = module {

    viewModelOf(::DetailViewModel)
}
