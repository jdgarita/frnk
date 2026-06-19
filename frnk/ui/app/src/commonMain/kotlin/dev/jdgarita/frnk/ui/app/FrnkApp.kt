package dev.jdgarita.frnk.ui.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.AppearanceController
import dev.jdgarita.frnk.ui.theme.FrnkTheme
import dev.jdgarita.frnk.ui.theme.LocalAppearanceController
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module

@OptIn(KoinExperimentalAPI::class)
@Composable
fun FrnkApp(
    onSavedStateConfiguration: () -> SavedStateConfiguration,
    onNavigationModule: (backStack: NavBackStack<NavKey>) -> Module
) {
    val appearanceController: AppearanceController = koinInject()

    CompositionLocalProvider(
        LocalAppearanceController provides appearanceController
    ) {
        val darkTheme =
            when (appearanceController.appearance) {
                Appearance.Light -> false
                Appearance.Dark -> true
                Appearance.System -> isSystemInDarkTheme()
            }

        ApplySystemBarAppearance(darkTheme = darkTheme)

        FrnkTheme {
            AppScaffold {
                val initialRoute = FrnkRoute.Onboarding

                val backStack =
                    rememberNavBackStack(
                        configuration = onSavedStateConfiguration(),
                        elements = arrayOf(initialRoute)
                    )

                remember(backStack) { loadKoinModules(module = onNavigationModule(backStack)) }

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.back() },
                    entryProvider = koinEntryProvider(),
                    entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                    transitionSpec = { defaultEnterTransition() },
                    popTransitionSpec = { defaultExitTransition() },
                    predictivePopTransitionSpec = { defaultExitTransition() }
                )
            }
        }
    }
}