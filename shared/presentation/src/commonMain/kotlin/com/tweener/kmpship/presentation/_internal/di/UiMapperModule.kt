package com.tweener.kmpship.presentation._internal.di

import com.tweener.kmpship.presentation.mapper.ToastMessageMapper
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 01/11/2023
 */

val uiMapperModule = module {

    factory { ToastMessageMapper() }

}
