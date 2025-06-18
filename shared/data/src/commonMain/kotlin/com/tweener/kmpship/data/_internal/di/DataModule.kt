package com.tweener.kmpship.data._internal.di

import com.tweener.kmpship.data.UserSyncScheduler
import com.tweener.kmpkit.provider.LocaleProvider
import com.tweener.kmpkit.provider.createLocaleProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 10/02/2024
 */
fun dataModule(isDebug: Boolean, versionName: String) = module {

    single<LocaleProvider> { createLocaleProvider() }

    includes(mapperModule)
    includes(dataSourceModule(isDebug = isDebug, versionName = versionName))
    includes(repositoryModule)

    singleOf(::UserSyncScheduler)
}
