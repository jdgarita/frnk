package dev.jdgarita.frnk.presentation._internal.di

import dev.jdgarita.frnk.presentation.mapper.ToastMessageMapper
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 01/11/2023
 */

val uiMapperModule = module {

    factory { ToastMessageMapper() }

}
