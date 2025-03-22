package dev.jdgarita.frnk.ui.identity.koin

import dev.garita.frnk.ui.framework.koin.navigationContextConfigsQualifier
import dev.garita.frnk.ui.framework.koin.navigationEventHandlersQualifier
import dev.garita.frnk.ui.framework.navigation.NavigationContextConfig
import dev.garita.frnk.ui.framework.navigation.NavigationEventHandler
import dev.jdgarita.frnk.ui.identity.navigation.SplashNavigationContextConfig
import dev.jdgarita.frnk.ui.identity.navigation.SplashNavigationEventHandler
import dev.jdgarita.frnk.util.di.KoinModuleProvider
import dev.jdgarita.frnk.util.di.intoSetSingle
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

class CommonIdentityUIKoinModuleProvider : KoinModuleProvider {
    override val modules: List<Module>
        get() = listOf(
            module {
                /*Event Handlers*/
                intoSetSingle<NavigationEventHandler, NavigationEventHandler>(
                    setQualifier = navigationEventHandlersQualifier,
                    valueQualifier = splashNavigationEventHandlersQualifier
                ) {
                    SplashNavigationEventHandler()
                }


                /*Context Configs*/
                intoSetSingle<SplashNavigationContextConfig, NavigationContextConfig>(
                    setQualifier = navigationContextConfigsQualifier
                ) {
                    SplashNavigationContextConfig()
                }
            }
        )
}

val splashNavigationEventHandlersQualifier = StringQualifier("SplashNavigationEventHandlersQualifier")