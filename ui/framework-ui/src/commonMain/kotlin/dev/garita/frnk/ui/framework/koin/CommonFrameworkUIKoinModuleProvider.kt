package dev.garita.frnk.ui.framework.koin

import dev.garita.frnk.ui.framework.navigation.NavigationContextConfig
import dev.garita.frnk.ui.framework.navigation.NavigationEventHandler
import dev.jdgarita.frnk.util.di.KoinModuleProvider
import dev.jdgarita.frnk.util.di.set
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

class CommonFrameworkUIKoinModuleProvider : KoinModuleProvider {

    override val modules: List<Module>
        get() = listOf(
            module {
                set<NavigationEventHandler>(navigationEventHandlersQualifier)
                set<NavigationContextConfig>(navigationContextConfigsQualifier)
            }
        )
}

val navigationContextConfigsQualifier = StringQualifier("NavigationContextConfigs")
val navigationEventHandlersQualifier = StringQualifier("NavigationEventHandlers")