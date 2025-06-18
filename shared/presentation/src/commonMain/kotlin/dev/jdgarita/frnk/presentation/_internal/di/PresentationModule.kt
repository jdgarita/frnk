package dev.jdgarita.frnk.presentation._internal.di

import dev.jdgarita.frnk.presentation._internal.dispatcher.ToastMessageDispatcher
import dev.jdgarita.frnk.presentation.screen.MainViewModel
import dev.jdgarita.frnk.presentation.screen.detail.di.detailModule
import dev.jdgarita.frnk.presentation.screen.favorites.di.favoritesModule
import dev.jdgarita.frnk.presentation.screen.home.di.homeModule
import dev.jdgarita.frnk.presentation.screen.profile.di.profileModule
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 10/02/2024
 */

val presentationModule = module {

    includes(uiMapperModule)

    singleOf(::ToastMessageDispatcher)

    viewModelOf(::MainViewModel)

    // Screens
    includes(homeModule)
    includes(favoritesModule)
    includes(profileModule)
    includes(detailModule)
}
