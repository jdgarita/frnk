package dev.jdgarita.frnk.sdk

import dev.garita.frnk.ui.framework.koin.CommonFrameworkUIKoinModuleProvider
import dev.jdgarita.frnk.domain.config.AppConfiguration
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationConfiguration
import dev.jdgarita.frnk.presentation.identity.koin.CommonIdentityPresentationKoinModuleProvider
import dev.jdgarita.frnk.presentation.mvi.koin.CommonPresentationMviKoinModuleProvider
import dev.jdgarita.frnk.ui.identity.koin.CommonIdentityUIKoinModuleProvider
import org.koin.core.module.Module

object CommonFrnkSdk : BaseCommonFrnkSdk() {

    private lateinit var externalModules: List<Module>

    override fun getModules(
        appConfiguration: AppConfiguration,
        navigationConfiguration: NavigationConfiguration
    ): List<Module> =
        CommonPresentationMviKoinModuleProvider().modules +
            CommonIdentityPresentationKoinModuleProvider().modules +
            CommonFrameworkUIKoinModuleProvider().modules +
            CommonIdentityUIKoinModuleProvider().modules +
            externalModules

    fun initialize(
        appConfiguration: AppConfiguration,
        navigationConfiguration: NavigationConfiguration,
        modules: List<Module> = emptyList()
    ) {
        externalModules = modules

        initialize(
            appConfiguration = appConfiguration,
            navigationConfiguration = navigationConfiguration
        )
    }
}