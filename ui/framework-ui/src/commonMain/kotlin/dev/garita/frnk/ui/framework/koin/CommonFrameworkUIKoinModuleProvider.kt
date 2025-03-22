package dev.garita.frnk.ui.framework.koin

import dev.jdgarita.frnk.util.di.KoinModuleProvider
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

class CommonFrameworkUIKoinModuleProvider : KoinModuleProvider {

    override val modules: List<Module>
        get() = listOf(
            module {

            }
        )
}

val navigationContextConfigsQualifier = StringQualifier("NavigationContextConfigs")