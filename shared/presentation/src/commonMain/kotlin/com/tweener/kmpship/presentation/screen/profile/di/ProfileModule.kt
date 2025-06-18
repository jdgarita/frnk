package com.tweener.kmpship.presentation.screen.profile.di

import com.tweener.kmpship.presentation.screen.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 19/02/2024
 */

val profileModule = module {

    viewModelOf(::ProfileViewModel)
}
