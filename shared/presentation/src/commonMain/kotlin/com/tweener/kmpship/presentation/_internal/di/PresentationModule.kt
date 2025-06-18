package com.tweener.kmpship.presentation._internal.di

import com.tweener.kmpship.presentation._internal.dispatcher.ToastMessageDispatcher
import com.tweener.kmpship.presentation.screen.MainViewModel
import com.tweener.kmpship.presentation.screen.detail.di.detailModule
import com.tweener.kmpship.presentation.screen.favorites.di.favoritesModule
import com.tweener.kmpship.presentation.screen.home.di.homeModule
import com.tweener.kmpship.presentation.screen.profile.di.profileModule
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
